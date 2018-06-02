package cc.aoeiuv020.base.jar

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.jayway.jsonpath.*
import com.jayway.jsonpath.spi.json.GsonJsonProvider
import com.jayway.jsonpath.spi.json.JsonProvider
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider
import com.jayway.jsonpath.spi.mapper.MappingProvider
import java.io.InputStream
import java.lang.reflect.Type

/**
 * Created by AoEiuV020 on 2018.05.13-13:27:12.
 */
val gson: Gson = GsonBuilder().create()

inline fun <reified T> type(): Type = object : TypeToken<T>() {}.type
fun Any?.toJson(gson: Gson): String = gson.toJson(this)
fun Any?.toJson(): String = gson.toJson(this)
// reified T 可以直接给gson用，没有reified的T用TypeToken包装也没用，只能传入type,
inline fun <reified T> String.toBean(gson: Gson): T = toBean(gson, type<T>())

inline fun <reified T> String.toBean(): T = toBean(gson, type<T>())

fun <T> String.toBean(gson: Gson, type: Type): T = gson.fromJson<T>(this, type)
fun <T> String.toNullableBean(gson: Gson, type: Type): T? = gson.fromJson<T>(this, type)

// 用到的地方都提前调用一下这个初始化，
fun gsonJsonPathInit() {
    // 重复设置无效，什么都不会发生，必须在第一次被使用前设置，
    Configuration.setDefaults(GsonJsonPathConfiguration)
}

inline fun <reified T> typeRef(): TypeRef<T> = object : TypeRef<T>() {}
fun <T> JsonElement.read(path: String, typeRef: TypeRef<T>): T = JsonPath.parse(this).read(path, typeRef)
val String.jsonPath: ReadContext get() = JsonPath.parse(this)
val InputStream.jsonPath: ReadContext get() = JsonPath.parse(this)
val JsonElement.jsonPath: ReadContext get() = JsonPath.parse(this)
// @是根节点，
inline fun <reified T> ReadContext.get(path: String = "@"): T = read(path, typeRef())
/*
inline fun <reified T> String.toBean(): T = JsonPath.parse(this).read("@", typeRef())
fun Any?.toJson(): String = JsonPath.parse(this).jsonString()
*/

object GsonJsonPathConfiguration : Configuration.Defaults {
    override fun jsonProvider(): JsonProvider = GsonJsonProvider(gson)
    override fun mappingProvider(): MappingProvider = GsonMappingProvider(gson)
    override fun options(): MutableSet<Option> = mutableSetOf()
}

