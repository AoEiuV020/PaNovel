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
        val str = """{"type":"cc.aoeiuv020.panovel.api.RequesterTest${'$'}SubRequester","extra":"hello"}"""
        val r = SubRequester("hello")
        val gson = GsonBuilder().paNovel().create()
        gson.toJson(r).let {
            assertEquals(str, it)
        }
        gson.fromJson(str, SubRequester::class.java).let {
            assertEquals(SubRequester::class, it::class)
            assertEquals("hello", it.url)
        }
    }

    @Suppress("unused")
    class SubRequester(url: String) : Requester(url) {
        var i: Int = 0
        var s: String = "str"
    }
}