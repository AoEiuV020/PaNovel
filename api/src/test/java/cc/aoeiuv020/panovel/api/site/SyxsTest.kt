package cc.aoeiuv020.panovel.api.site

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 *
 * Created by AoEiuV020 on 2018.03.14-02:19:11.
 */
class SyxsTest {
    init {
        System.setProperty("org.slf4j.simpleLogger.log.Syxs", "trace")
    }

    private lateinit var context: Syxs
    @Before
    fun setUp() {
        context = Syxs()
    }

    @Test
    fun getNovelDetail() {
        context.getNovelDetail("http://www.31xs.net/5/5821/").let {
            assertEquals("天刑纪", it.novel.name)
            assertEquals("曳光", it.novel.author)
            assertEquals("今朝修仙不为仙，只为春色花满园：来日九星冲牛斗，且看天刑开纪元。", it.introduction)
            assertEquals("http://www.31xs.net/img/5/5821/5821s.jpg", it.bigImg)
            println(it.update)
        }
    }
}