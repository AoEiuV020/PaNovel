package cc.aoeiuv020.panovel.api

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
    fun getNovelList() {
        context.getNovelList(GenreListRequester("http://www.31xs.org/list/1/")).let {
            it.forEach { novelItem ->
                println(novelItem)
            }
            assertEquals(50, it.size)
        }
    }

}