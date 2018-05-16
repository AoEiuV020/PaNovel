package cc.aoeiuv020.panovel

import org.junit.Assert.assertTrue
import org.junit.Test

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
}