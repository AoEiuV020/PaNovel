package cc.aoeiuv020.panovel

import android.support.test.runner.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 测试正则是否需要escape右中括号，
 * 之前爬漫画时遇到这个在最新android api 25模拟器上必须转义escape，而pc java又都可以，然后as警告建议不转义，
 * 之后我就习惯顶着警告写这个转义，
 * 突然想起这个问题，试一下，结果居然正常，不需要转义，
 * Created by AoEiuV020 on 2018.05.16-19:57:03.
 */
@RunWith(AndroidJUnit4::class)
class RegexInstrumentedTest {
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