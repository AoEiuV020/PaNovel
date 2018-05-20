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
        context.getNovelText("http://www.76wx.com/book/5017/2760870.html").let {
            it.textList.forEach {
                println(it)
            }
        }
    }
}