package cc.aoeiuv020.base.jar

import cc.aoeiuv020.gson.GsonUtils
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.jayway.jsonpath.*
import com.jayway.jsonpath.spi.json.GsonJsonProvider
import com.jayway.jsonpath.spi.json.JsonProvider
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider
import com.jayway.jsonpath.spi.mapper.MappingProvider
import java.io.InputStream

/**
 * Created by AoEiuV020 on 2018.05.13-13:27:12.
 */
private val gson: Gson = GsonUtils.gson

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
    override fun jsonProvider(): JsonProvider = GsonJsonProvider(GsonUtils.gson)
    override fun mappingProvider(): MappingProvider = GsonMappingProvider(gson)
    override fun options(): MutableSet<Option> = mutableSetOf()
}

