package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.panovel.api.NovelChapter
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Test
import java.lang.reflect.Type

/**
 *
 * Created by AoEiuV020 on 2017.10.12-11:16:47.
 */
@Suppress("UNCHECKED_CAST")
class CacheTest {
    @Test
    fun type() {
        var type: Type = object : TypeToken<String>() {}.type
        assertEquals(String::class.java, type)
        type = object : TypeToken<List<String>>() {}.type
        assertEquals("com.google.gson.internal.\$Gson\$Types\$ParameterizedTypeImpl", type.javaClass.name)
        assertEquals("java.util.List<? extends java.lang.String>", type.toString())
        val list: List<String> = listOf("")
        type = list.javaClass
        assertEquals("class java.util.Collections\$SingletonList", type.toString())

        t<List<NovelChapter>>()
    }

    private fun <T> t() {
        val type: Type = object : TypeToken<T>() {}.type
        assertEquals("sun.reflect.generics.reflectiveObjects.TypeVariableImpl", type.javaClass.name)
        assertEquals("T", type.toString())
    }
}