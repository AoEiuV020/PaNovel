package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.DetailRequester
import cc.aoeiuv020.panovel.api.GenreListRequester
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
    fun getGenres() {
        val genres = context.getGenres()
        genres[0].let {
            assertEquals("玄幻", it.name)
            assertEquals("http://www.31xs.org/list/1/", it.requester.url)
        }
        genres[genres.size - 1].let {
            assertEquals("同人", it.name)
            assertEquals("http://www.31xs.org/list/13/", it.requester.url)
        }
    }

    @Test
    fun getNovelDetail() {
        context.getNovelDetail(DetailRequester("http://www.31xs.org/5/5821/")).let {
            assertEquals("天刑纪", it.novel.name)
            assertEquals("曳光", it.novel.author)
            assertEquals("今朝修仙不为仙，只为春色花满园：来日九星冲牛斗，且看天刑开纪元。", it.introduction)
            assertEquals("http://www.31xs.org/img/5/5821/5821s.jpg", it.bigImg)
            println(it.update)
        }
    }

    @Test
    fun getNovelList() {
        context.getNovelList(GenreListRequester("http://www.31xs.org/list/1/")).let {
            it.forEach { novelItem ->
                println(novelItem)
            }
            assertEquals(50, it.size)
        }
    }

}