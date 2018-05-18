package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.Requester
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
        context.getNovelChaptersAsc(Requester("http://www.76wx.com/book/454/")).let {
            val firstChapter = it.first()
            assertEquals("http://www.76wx.com/book/454/277839.html", firstChapter.requester.url)
        }
    }

    @Test
    fun getNovelText() {
        context.getNovelText(Requester("http://www.76wx.com/book/5017/2760870.html")).let {
            it.textList.forEach {
                println(it)
            }
        }
    }
}