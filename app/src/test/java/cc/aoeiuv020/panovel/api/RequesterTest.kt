package cc.aoeiuv020.panovel.api

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 *
 * Created by AoEiuV020 on 2017.10.07-17:16:28.
 */
class RequesterTest {
    private val subRequesterString = "cc.aoeiuv020.panovel.api.RequesterTest\$SubRequester/hello"
    @Test
    fun toStringTest() {
        val r = SubRequester("hello")
        r.i = 10
        r.s = "rts"
        val s = Requester.serialize(r)
        assertEquals(subRequesterString, s)
    }

    @Test
    fun fromStringTest() {
        val r: SubRequester = Requester.deserialize(subRequesterString)
        assertEquals(0, r.i)
        assertEquals("str", r.s)
        assertEquals("hello", r.url)
    }

    @Test
    fun emptyExtraTest() {
        val r: SubRequester = Requester.deserialize(Requester.serialize(SubRequester("")))
        assertEquals("", r.url)
    }

    class SubRequester(url: String) : Requester(url) {
        var i: Int = 0
        var s: String = "str"
    }
}