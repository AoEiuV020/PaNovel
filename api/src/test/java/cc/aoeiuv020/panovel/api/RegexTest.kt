package cc.aoeiuv020.panovel.api

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by AoEiuV020 on 2018.06.03-12:29:01.
 */
class RegexTest {
    @Test
    fun group() {
        "https://www.gxwztv.com/55/55886/16079406.html"
                .replace(Regex("/(\\d+)/(\\d+)/(\\d+)"), "\\1,\\2,\\3")
                .let {
                    assertEquals("https://www.gxwztv.com1,2,3.html", it)
                }
    }
}