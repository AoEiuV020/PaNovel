package cc.aoeiuv020.panovel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.regex.Pattern

/**
 * Created by AoEiuV020 on 2018.05.16-19:57:13.
 */
class RegexUnitTest {
    private val withoutEscapeRegex = Regex("]")
    private val withEscapeRegex = Regex("\\]")
    private val str = "]"
    @Test
    fun withTest() {
        assertTrue(str.matches(withEscapeRegex))
    }

    @Test
    fun withoutTest() {
        assertTrue(str.matches(withoutEscapeRegex))
    }

    /**
     * 正则里\Q和\E之间的所有不转义，
     */
    @Test
    fun quote() {
        val str = "[]"
        val quoteStr = Pattern.quote(str)
        assertEquals("\\Q[]\\E", quoteStr)
        assertTrue(str.matches(Regex(quoteStr)))
    }
}