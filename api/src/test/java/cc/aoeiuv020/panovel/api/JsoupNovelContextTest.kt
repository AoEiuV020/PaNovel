package cc.aoeiuv020.panovel.api

import cc.aoeiuv020.panovel.api.base.JsoupNovelContext
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Created by AoEiuV020 on 2018.05.20-12:16:43.
 */
class JsoupNovelContextTest {
    @Test
    fun singleP() {
        // https://www.biqubao.com/book/18156/
        val html = """
<p>制作游戏成功的林陨意外猝死，穿越到自己制作的游戏世界，结果拥有了这个世界的最大权限！<br>　　最大权限书友群：305908807</p>"""
        val root = Jsoup.parse(html)
        root.select("body > p").testList().let {
            assertEquals(2, it.size)
            assertEquals("制作游戏成功的林陨意外猝死，穿越到自己制作的游戏世界，结果拥有了这个世界的最大权限！", it[0])
            assertEquals("最大权限书友群：305908807", it[1])
        }
    }

    private fun Elements.testList(): List<String> = flatMap {
        JsoupNovelContext.textList(it)
    }

    @Test
    fun trimTest() {
        val text = """
	　  　
"""
        val trimed = text.trim()
        assertEquals(0, trimed.length)
    }

    @Test
    fun isWhitespace() {
        val text = """
	 　
"""
        text.forEach {
            assertTrue(it.isWhitespace())
        }
    }

    @Test
    fun regexWhitespace() {
        val text = """
	  　
"""
        text.split(Regex("[\\p{javaWhitespace}\\p{javaSpaceChar}]+")).let {
            assertEquals(2, it.size)
            it.forEach {
                assertTrue(it.isEmpty())
            }
        }
    }

    @Test
    fun split() {
        val whitespaceRegex = Regex("[\\p{javaWhitespace}\\p{javaSpaceChar}]+")
        val str = "    中年男子下意识的接住魂晶，这东西能够让卡片使更好的修炼魂力，可以说是硬通货，属于最高等的金钱。"
        str.split(whitespaceRegex).let {
            assertEquals(2, it)
            assertTrue(it[0].isEmpty())
            assertEquals("中年男子下意识的接住魂晶，这东西能够让卡片使更好的修炼魂力，可以说是硬通货，属于最高等的金钱。", it[1])
        }
    }

    @Test
    fun jsoupSelect() {
        val html = """
<div id="intro">
				<p>制作游戏成功的林陨意外猝死，穿越到自己制作的游戏世界，结果拥有了这个世界的最大权限！<br>　　最大权限书友群：305908807</p>
				<p>各位书友要是觉得《最大权限》还不错的话请不要忘记向您QQ群和微博里的朋友推荐哦！</p>
			</div>
"""
        val root = Jsoup.parse(html)
        root.select("#intro > p:not(:nth-last-child(1))").let {
            assertEquals(1, it.size)
            assertEquals("<p>制作游戏成功的林陨意外猝死，穿越到自己制作的游戏世界，结果拥有了这个世界的最大权限！<br>　　最大权限书友群：305908807</p>", it.outerHtml())
        }
    }
}