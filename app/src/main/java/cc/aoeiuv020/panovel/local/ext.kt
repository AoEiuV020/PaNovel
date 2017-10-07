@file:Suppress("unused")

package cc.aoeiuv020.panovel.local

import android.util.Base64
import cc.aoeiuv020.panovel.App
import cc.aoeiuv020.panovel.api.NovelDetail
import cc.aoeiuv020.panovel.api.NovelItem
import java.io.*
import kotlin.reflect.KProperty

/**
 *
 * Created by AoEiuV020 on 2017.10.04-15:33:13.
 */

class ContextDelegate<T : Serializable>(private val default: T) {
    operator fun getValue(thisRef: LocalSource, property: KProperty<*>): T {
        return thisRef.fileLoad(property.name) ?: default
    }

    operator fun setValue(thisRef: LocalSource, property: KProperty<*>, value: T) {
        thisRef.fileSave(property.name, value)
    }
}

class NullableContextDelegate<T : Serializable>(private val default: T? = null) {
    operator fun getValue(thisRef: LocalSource, property: KProperty<*>): T? {
        return thisRef.fileLoad(property.name) ?: default
    }

    operator fun setValue(thisRef: LocalSource, property: KProperty<*>, value: T?) {
        thisRef.fileSave(property.name, value)
    }
}

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
