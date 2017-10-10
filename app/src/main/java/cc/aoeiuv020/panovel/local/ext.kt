@file:Suppress("unused")

package cc.aoeiuv020.panovel.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import cc.aoeiuv020.panovel.App
import cc.aoeiuv020.panovel.api.GsonSerializable
import cc.aoeiuv020.panovel.api.NovelDetail
import cc.aoeiuv020.panovel.api.NovelItem
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.Serializable
import java.lang.reflect.Type

/**
 *
 * Created by AoEiuV020 on 2017.10.04-15:33:13.
 */

private fun LocalSource.external(fileType: FileType) = File(File(App.ctx.getExternalFilesDir(null), fileType.name.toLowerCase()), this.javaClass.simpleName)
        .apply { mkdirs() }

private fun LocalSource.file(file: File, name: String) = File(file, name).apply { parentFile.mkdirs() }

enum class FileType {
    PRIMITIVE, GSON
}

private fun LocalSource.externalPrimitive() = external(FileType.PRIMITIVE)
private fun LocalSource.externalPrimitive(name: String) = file(externalPrimitive(), name)
fun LocalSource.primitiveSave(name: String, obj: Serializable?) {
    obj?.let { externalPrimitive(name).writeText(App.gson.toJson(it)) }
            ?: primitiveRemove(name)
}


@Suppress("UNCHECKED_CAST")
fun <T> LocalSource.primitiveLoad(file: File): T? = try {
    // 这传给gson的T没用，但是可以读出原始类型，
    App.gson.fromJson(file.readText(), object : TypeToken<T>() {}.type)
} catch (_: Exception) {
    file.delete()
    null
}

fun <T> LocalSource.primitiveLoad(name: String): T? = primitiveLoad(externalPrimitive(name))
fun LocalSource.primitiveExists(name: String): Boolean = externalPrimitive(name).exists()
fun LocalSource.primitiveRemove(name: String): Boolean = externalPrimitive(name).delete()
@Suppress("UNCHECKED_CAST")
fun <T : Any> LocalSource.primitiveList(): List<T> = externalPrimitive().listFiles().mapNotNull { file ->
    primitiveLoad<T>(file)
}

val NovelDetail.bookId get() = novel.bookId
val NovelItem.bookId get() = "$name.$author.$site"

fun md5Base64(s: String): String {
    val digest = java.security.MessageDigest.getInstance("MD5")
    digest.update(s.toByteArray())
    val messageDigest = digest.digest()
    return Base64.encodeToString(messageDigest, Base64.NO_PADDING or Base64.URL_SAFE or Base64.NO_WRAP)
}

fun LocalSource.pref(): SharedPreferences = App.ctx.getSharedPreferences(this.javaClass.name, Context.MODE_PRIVATE)
fun LocalSource.prefSave(key: String, value: String) = pref().edit().putString(key, value).apply()
fun LocalSource.prefLoad(key: String): String? = pref().getString(key, null)
fun LocalSource.prefRemove(key: String) = pref().edit().putString(key, null).apply()

inline fun <reified T : GsonSerializable> type(): Type = object : TypeToken<T>() {}.type
private fun LocalSource.externalGson() = external(FileType.GSON)
private fun LocalSource.externalGson(name: String) = file(externalGson(), name)
fun LocalSource.gsonExists(name: String): Boolean = externalGson(name).exists()
fun LocalSource.gsonRemove(name: String): Boolean = externalGson(name).delete()
fun LocalSource.gsonSave(name: String, obj: GsonSerializable?) {
    obj?.let { externalGson(name).writeText(it.toJson()) }
            ?: gsonRemove(name)
}

fun <T : GsonSerializable> LocalSource.gsonLoad(name: String, type: Type): T? = gsonLoad(externalGson(name), type)
inline fun <reified T : GsonSerializable> LocalSource.gsonLoad(name: String): T? = gsonLoad(name, type<T>())
fun <T : GsonSerializable> LocalSource.gsonLoad(file: File, type: Type): T? = try {
    file.readText().toBean(type)
} catch (_: Exception) {
    file.delete()
    null
}

fun <T : GsonSerializable> LocalSource.gsonList(type: Type) = externalGson().listFiles().mapNotNull { file ->
    gsonLoad<T>(file, type)
}

inline fun <reified T : GsonSerializable> LocalSource.gsonList(): List<T> = gsonList(type<T>())

fun GsonSerializable.toJson(): String = App.gson.toJson(this)
// reified T 可以直接给gson用，没有reified的T用TypeToken包装也没用，只能传入type,
inline fun <reified T : GsonSerializable> String.toBean(): T = App.gson.fromJson(this, type<T>())

fun <T : GsonSerializable> String.toBean(type: Type): T = App.gson.fromJson<T>(this, type)
