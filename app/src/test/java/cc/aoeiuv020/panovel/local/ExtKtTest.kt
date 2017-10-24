package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.panovel.api.NovelContext
import cc.aoeiuv020.panovel.api.NovelSite
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.lang.reflect.Type

/**
 *
 * Created by AoEiuV020 on 2017.10.04-16:35:10.
 */
class ExtKtTest {
    @Test
    fun writeObjectNullTest() {
        val bos = ByteArrayOutputStream()
        ObjectOutputStream(bos).apply {
            writeObject(null)
            close()
        }
        val bis = ByteArrayInputStream(bos.toByteArray())
        ObjectInputStream(bis).apply {
            val obj = readObject()
            close()
            assertNull(obj)
            val s = obj as? String
            assertNull(s)
        }
    }

    private val gson: Gson = GsonBuilder().create()
    @Test
    fun gsonPrimitive() {
        gsonPrimitiveAssert("hello")
        gsonPrimitiveAssert(888)
        gsonPrimitiveAssert(888.888)
        gsonPrimitiveAssert(false)
        gsonPrimitiveAssert('k')
    }

    private fun gsonPrimitiveAssert(obj: Any) {
        assertEquals(obj, gson.fromJson(gson.toJson(obj), obj::class.java))
    }

    @Test
    fun toBean() {
        val str = "{\"name\":\"飘天文学\",\"baseUrl\":\"http://www.piaotian.com/\",\"logo\":\"http://www.piaotian.com/css/logo.gif\"}"
        val item = NovelContext.getNovelContextByName("飘天文学").getNovelSite()
        item.toJson().let {
            assertEquals(str, it)
        }
        str.toBean<NovelSite>().let {
            assertEquals(item, it)
        }
    }

    @Test
    fun gsonTypeTest() {
        val type = gsonType<String>()
        println(type)
    }

    private fun <T> gsonType(): Type = object : TypeToken<T>() {}.type
}

