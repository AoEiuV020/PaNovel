@file:Suppress("unused")

package cc.aoeiuv020.panovel.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import cc.aoeiuv020.panovel.App
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.NovelItem
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.Serializable
import java.lang.reflect.Type

/**
 *
 * Created by AoEiuV020 on 2017.10.04-15:33:13.
 */

@Suppress("UNUSED_PARAMETER")
private fun LocalSource.external(fileType: FileType) = File(App.ctx.getExternalFilesDir(null), this.path)
        .apply { mkdirs() }

private fun LocalSource.folder(file: File, folder: String? = null) = (folder?.let { File(file, folder) } ?: file).apply { mkdirs() }
private fun LocalSource.file(file: File, name: String, folder: String? = null) = File(folder(file, folder), name)

enum class FileType {
    PRIMITIVE, GSON, FILE
}

private fun LocalSource.externalPrimitive(folder: String? = null) = folder(external(FileType.PRIMITIVE), folder)
private fun LocalSource.externalPrimitive(name: String, folder: String? = null) = file(externalPrimitive(folder), name)
fun LocalSource.primitiveSave(name: String, obj: Serializable?, folder: String? = null) {
    obj?.let { externalPrimitive(name, folder).writeText(App.gson.toJson(it)) }
            ?: primitiveRemove(name, folder)
}


@Suppress("UNCHECKED_CAST")
fun <T> LocalSource.primitiveLoad(file: File): T? = try {
    // 这传给gson的T没用，但是可以读出原始类型，
    App.gson.fromJson(file.readText(), object : TypeToken<T>() {}.type)
} catch (_: Exception) {
    file.delete()
    null
}

fun <T> LocalSource.primitiveLoad(name: String, folder: String? = null): T? = primitiveLoad(externalPrimitive(name, folder))
fun LocalSource.primitiveExists(name: String, folder: String? = null): Boolean = externalPrimitive(name, folder).exists()
fun LocalSource.primitiveRemove(name: String, folder: String? = null): Boolean = externalPrimitive(name, folder).delete()
@Suppress("UNCHECKED_CAST")
fun <T : kotlin.Any> LocalSource.primitiveList(folder: String? = null): List<T> = externalPrimitive(folder).listFiles().mapNotNull { file ->
    primitiveLoad<T>(file)
}

val NovelItem.bookId get() = NovelId(site, author, name)
val NovelChapter.id get() = name + '.' + md5Base64(requester.extra)

fun md5Base64(s: String): String {
    val digest = java.security.MessageDigest.getInstance("MD5")
    digest.update(s.toByteArray())
    val messageDigest = digest.digest()
    return Base64.encodeToString(messageDigest, Base64.NO_PADDING or Base64.URL_SAFE or Base64.NO_WRAP)
}

fun LocalSource.pref(): SharedPreferences = App.ctx.getSharedPreferences(this.path, Context.MODE_PRIVATE)
fun LocalSource.prefSave(key: String, value: String) = pref().edit().putString(key, value).apply()
fun LocalSource.prefLoad(key: String): String? = pref().getString(key, null)
fun LocalSource.prefRemove(key: String) = pref().edit().putString(key, null).apply()

inline fun <reified T> type(): Type = object : TypeToken<T>() {}.type
private fun LocalSource.externalGson(folder: String? = null) = folder(external(FileType.GSON), folder)
private fun LocalSource.externalGson(name: String, folder: String? = null) = file(externalGson(folder), name)
fun LocalSource.gsonExists(name: String, folder: String? = null): Boolean = externalGson(name, folder).exists()
fun LocalSource.gsonRemove(name: String, folder: String? = null): Boolean = externalGson(name, folder).delete()
fun LocalSource.gsonSave(name: String, obj: Any?, folder: String? = null) {
    obj?.let { externalGson(name, folder).writeText(it.toJson()) }
            ?: gsonRemove(name, folder)
}

fun <T> LocalSource.gsonLoad(name: String, type: Type, refreshTime: Long = 0, folder: String? = null): T? = gsonLoad(externalGson(name, folder), type, refreshTime)
inline fun <reified T> LocalSource.gsonLoad(name: String, refreshTime: Long = 0, folder: String? = null): T? = gsonLoad(name, type<T>(), refreshTime, folder)
fun <T> LocalSource.gsonLoad(file: File, type: Type, refreshTime: Long = 0): T? = try {
    // refreshTime 之后保存的数据才取出，
    file.takeIf { it.lastModified() >= refreshTime }?.run {
        readText().toBean(type)
    }
} catch (_: Exception) {
    file.delete()
    null
}

// 这个mapNotNull要求T : Any，不能删掉: Any,
fun <T : Any> LocalSource.gsonList(type: Type, folder: String? = null) = externalGson(folder).listFiles().mapNotNull { file ->
    gsonLoad<T>(file, type)
}

inline fun <reified T : Any> LocalSource.gsonList(folder: String? = null): List<T> = gsonList(type<T>(), folder)

fun LocalSource.gsonNameList(folder: String? = null) = externalGson(folder).listFiles().map { file ->
    file.name.let { name ->
        name.substring(name.lastIndexOf(File.separatorChar) + 1)
    }
}

fun LocalSource.gsonClear() = externalGson().deleteRecursively()

fun Any.toJson(): String = App.gson.toJson(this)
// reified T 可以直接给gson用，没有reified的T用TypeToken包装也没用，只能传入type,
inline fun <reified T> String.toBean(): T = App.gson.fromJson(this, type<T>())

fun <T> String.toBean(type: Type): T = App.gson.fromJson<T>(this, type)

private fun LocalSource.externalFile(folder: String? = null) = folder(external(FileType.FILE), folder)
private fun LocalSource.externalFile(name: String, folder: String? = null) = file(externalFile(folder), name)
fun LocalSource.openFile(name: String, folder: String? = null) = externalFile(name, folder)
