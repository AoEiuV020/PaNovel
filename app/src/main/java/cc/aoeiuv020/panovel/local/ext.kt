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

private fun LocalSource.external(fileType: FileType) = File(File(App.ctx.getExternalFilesDir(null), fileType.name.toLowerCase()), this.javaClass.name).apply { mkdirs() }

enum class FileType {
    PRIMITIVE, GSON
}

private fun LocalSource.externalPrimitive(name: String) = File(external(FileType.PRIMITIVE), name)
fun LocalSource.primitiveSave(name: String, obj: Serializable?) {
    if (obj == null) {
        primitiveRemove(name)
        return
    } else {
        externalPrimitive(name).writeText(App.gson.toJson(obj))
    }
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
fun <T : Any> LocalSource.primitiveList(): List<T> = external(FileType.PRIMITIVE).listFiles().mapNotNull { file ->
    primitiveLoad<T>(file)
}

val NovelDetail.bookId get() = novel.bookId
val NovelItem.bookId get() = md5Base64(toString())

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
private fun LocalSource.externalGson(name: String) = File(external(FileType.GSON), name)
fun LocalSource.gsonExists(name: String): Boolean = externalGson(name).exists()
fun LocalSource.gsonRemove(name: String): Boolean = externalGson(name).delete()
fun LocalSource.gsonSave(name: String, obj: GsonSerializable?) {
    if (obj == null) {
        gsonRemove(name)
        return
    } else {
        externalGson(name).writeText(obj.toJson())
    }
}

fun <T : GsonSerializable> LocalSource.gsonLoad(name: String, type: Type): T? = gsonLoad(externalGson(name), type)
inline fun <reified T : GsonSerializable> LocalSource.gsonLoad(name: String): T? = gsonLoad(name, type<T>())
fun <T : GsonSerializable> LocalSource.gsonLoad(file: File, type: Type): T? = try {
    file.readText().toBean(type)
} catch (_: Exception) {
    file.delete()
    null
}

inline fun <reified T : GsonSerializable> LocalSource.gsonList(): List<T> = primitiveList<String>().map { it.toBean<T>() }
fun GsonSerializable.toJson(): String = App.gson.toJson(this)
// reified T 可以直接给gson用，没有reified的T用TypeToken包装也没用，只能传入type,
inline fun <reified T : GsonSerializable> String.toBean(): T = App.gson.fromJson(this, type<T>())

fun <T : GsonSerializable> String.toBean(type: Type): T = App.gson.fromJson<T>(this, type)
