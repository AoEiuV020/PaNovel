@file:Suppress("unused")

package cc.aoeiuv020.panovel.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import cc.aoeiuv020.panovel.App
import cc.aoeiuv020.panovel.api.NovelDetail
import cc.aoeiuv020.panovel.api.NovelItem
import java.io.*

/**
 *
 * Created by AoEiuV020 on 2017.10.04-15:33:13.
 */

private fun LocalSource.externalFile() = File(File(App.ctx.getExternalFilesDir(null), "ext"), this.javaClass.name).apply { mkdirs() }
fun LocalSource.fileSave(name: String, obj: Serializable?) {
    ObjectOutputStream(FileOutputStream(File(externalFile(), name))).run {
        writeObject(obj)
        close()
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> LocalSource.fileLoad(file: File): T? = try {
    ObjectInputStream(FileInputStream(file)).run {
        (readObject() as? T).also { close() }
    }
} catch (_: Exception) {
    file.delete()
    null
}

fun <T> LocalSource.fileLoad(name: String): T? = fileLoad(File(externalFile(), name))

fun LocalSource.fileExists(name: String): Boolean = File(externalFile(), name).exists()
fun LocalSource.fileRemove(name: String): Boolean = File(externalFile(), name).delete()
@Suppress("UNCHECKED_CAST")
fun <T : Any> LocalSource.fileList(): List<T> = externalFile().listFiles().mapNotNull { file ->
    fileLoad<T>(file)
}

val NovelDetail.bookId get() = novel.bookId
val NovelItem.bookId get() = md5Base64(toString())

fun md5Base64(s: String): String {
    val digest = java.security.MessageDigest.getInstance("MD5")
    digest.update(s.toByteArray())
    val messageDigest = digest.digest()
    return Base64.encodeToString(messageDigest, Base64.NO_PADDING or Base64.URL_SAFE or Base64.NO_WRAP)
}

fun LocalSource.pref(name: String): SharedPreferences = App.ctx.getSharedPreferences(name, Context.MODE_PRIVATE)
fun LocalSource.prefSave(name: String, data: Map<String, String>) {
    pref(name).edit().apply {
        data.forEach {
            putString(it.key, it.value)
        }
    }.apply()
}

fun LocalSource.prefLoad(name: String): Map<String, String> = pref(name).all.mapValues { it.value.toString() }

fun LocalSource.prefRemove(name: String) = pref(name).edit().clear().apply()
