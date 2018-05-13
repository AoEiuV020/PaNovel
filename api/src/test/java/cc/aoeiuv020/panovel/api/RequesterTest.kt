package cc.aoeiuv020.panovel.api

import com.google.gson.GsonBuilder
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 *
 * Created by AoEiuV020 on 2017.10.07-17:16:28.
 */
class RequesterTest {
    @Test
    fun gson() {
        val str = """".RequesterTest${'$'}SSRequester|hello""""
        val r = SSRequester("hello")
        val gson = GsonBuilder().paNovel().create()
        gson.toJson(r).let {
            assertEquals(str, it)
        }
        gson.fromJson(str, SubRequester::class.java).let {
            assertEquals(SSRequester::class, it::class)
            assertEquals("hello", it.url)
        }
    }

    @Test
    fun old() {
        val str = """{"type":"cc.aoeiuv020.panovel.api.RequesterTest${'$'}SubRequester","extra":"hello"}"""
        val gson = GsonBuilder().paNovel().create()
        gson.fromJson(str, SubRequester::class.java).let {
            assertEquals(SubRequester::class, it::class)
            assertEquals("hello", it.url)
        }
    }


    @Test
    fun wrap() {
        val str = """{"requester":".RequesterTest${'$'}SSRequester|hello"}"""
        val r = SSRequester("hello")
        val w = WrapRequester(r)
        val gson = GsonBuilder().paNovel().create()
        gson.toJson(w).let {
            assertEquals(str, it)
        }
        gson.fromJson(str, WrapRequester::class.java).let {
            assertEquals(WrapRequester::class, it::class)
            assertEquals("hello", it.requester.url)
        }
    }

    @Test
    fun testPackage() {
        WrapRequester::class.java.let {
            assertEquals("cc.aoeiuv020.panovel.api.RequesterTest\$WrapRequester", it.name)
            assertEquals("cc.aoeiuv020.panovel.api.RequesterTest.WrapRequester", it.canonicalName)
            assertEquals("WrapRequester", it.simpleName)
            assertEquals("cc.aoeiuv020.panovel.api", it.`package`.name)
        }
    }

    @Suppress("unused")
    open class SubRequester(url: String) : Requester(url) {
        var i: Int = 0
        var s: String = "str"
    }

    class SSRequester(url: String) : SubRequester(url)

    data class WrapRequester(
            val requester: SubRequester
    )
}