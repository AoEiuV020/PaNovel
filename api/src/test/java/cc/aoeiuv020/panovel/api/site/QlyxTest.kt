package cc.aoeiuv020.panovel.api.site

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Created by AoEiuV020 on 2018.05.10-22:13:32.
 */
class QlyxTest {
    init {
        System.setProperty("org.slf4j.simpleLogger.log.Qlyx", "trace")
    }

    private lateinit var context: Qlyx
    @Before
    fun setUp() {
        context = Qlyx()
    }

    @Test
    fun getNovelChaptersAsc() {
        context.getNovelChaptersAsc("http://www.76wx.com/book/454/").let {
            val firstChapter = it.first()
            assertEquals("第一章 黄山真君和九洲一号群", firstChapter.name)
            assertEquals("454/277839", firstChapter.extra)
        }
    }

    @Test
    fun reverseRemoveTest() {
        val list = listOf(9, 8, 7, 1, 2, 3, 4, 5, 6, 7, 8, 9)
        reverseRemove(list).let {
            println(it)
        }
    }

    private fun reverseRemove(list: List<Int>): List<Int> {
        var index = 0
        // 以防万一，
        if (list.size == 1) return list
        // 倒序列表判断是否重复章节，
        val reversedList = list.asReversed()
        return list.dropWhile {
            @Suppress("DEPRECATED_IDENTITY_EQUALS")
            (it === reversedList[index]).also { ++index }
        }
    }

    @Test
    fun getNovelText() {
        context.getNovelText("http://www.76wx.com/book/5017/2760870.html").textList.let {
            assertEquals(58, it.size)
            assertEquals("林远凡“活”了之后的几天一直都躺在重症监护室中，各种进口仪器的感应器布满了他身体，头上打着绷带，骨折的手臂也用夹板固定住了。", it.first())
            assertEquals("“先找个灵气稍微丰富的地方修炼一番，踏入炼气期再说，要是有什么天材地宝就更好了。”想到这林远凡不禁摇了摇头，地球灵气如此稀薄，人类众多，怕是年份稍长一些的药材都被采完了，就算是有也不是现在的自己能弄到手的。", it.last())
        }
    }
}