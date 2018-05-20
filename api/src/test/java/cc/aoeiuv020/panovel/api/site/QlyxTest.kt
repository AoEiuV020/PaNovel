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
            assertEquals("/book/454/277839.html", firstChapter.extra)
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