package cc.aoeiuv020.panovel.api.site

import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * // TODO: 有的小说章节列表不用扔掉开头， https://www.yssm.org/uctxt/294/294596/
 * Created by AoEiuV020 on 2018.05.18-16:51:57.
 */
class YssmTest {
    init {
        System.setProperty("org.slf4j.simpleLogger.log.Yssm", "trace")
    }

    private lateinit var context: Yssm
    @Before
    fun setUp() {
        context = Yssm()
    }

    @Test
    fun getNovelList() {
        context.searchNovelName("都市").forEach {
            println(it)
        }
    }

    @Test
    fun getNovelChaptersAsc() {
        context.getNovelChaptersAsc("https://www.yssm.org/uctxt/294/294596/").let {
            assertTrue(it.isNotEmpty())
            println(it.size)
            println(it.first())
        }
    }
}