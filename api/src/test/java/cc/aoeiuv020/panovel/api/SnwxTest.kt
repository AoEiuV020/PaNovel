package cc.aoeiuv020.panovel.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 *
 * Created by AoEiuV020 on 2018.03.07-03:24:15.
 */
class SnwxTest {
    init {
        System.setProperty("org.slf4j.simpleLogger.log.SnwxTest", "trace")
    }

    private lateinit var context: Snwx
    @Before
    fun setUp() {
        context = Snwx()
    }

    @Test
    fun getGenres() {
        val genres = context.getGenres()
        genres[0].let {
            assertEquals("玄幻小说", it.name)
            assertEquals("https://www.snwx8.com/sort1/1.html", it.requester.url)
        }
        genres[genres.size - 1].let {
            assertEquals("全本小说", it.name)
            assertEquals("https://www.snwx8.com/quanben/", it.requester.url)
        }
    }

    @Test
    fun getNextPage() {
        context.getNextPage(NovelGenre("玄幻小说", "https://www.snwx8.com/sort1/1.html")).let {
            assertTrue(it!!.requester.url.endsWith("2.html"))
        }
    }

    @Test
    fun getNovelList() {
        context.getNovelList(GenreListRequester("https://www.snwx8.com/sort1/1.html")).let {
            it.forEach { novelItem ->
                println(novelItem)
            }
            assertEquals(65, it.size)
        }
        context.getNovelList(GenreListRequester("https://www.snwx8.com/sort1/2.html")).let {
            it.forEach { novelItem ->
                println(novelItem)
            }
            assertEquals(29, it.size)
        }
        context.getNovelList(GenreListRequester("https://www.snwx8.com/quanben/")).let {
            it.forEach { novelItem ->
                println(novelItem)
            }
            assertEquals(59, it.size)
        }
    }

    @Test
    fun searchNovelName() {
        context.getNovelList(context.searchNovelName("祖魔").requester).let {
            it.forEach { novelItem ->
                println(novelItem)
            }
            assertTrue(it.any { novelItem ->
                novelItem.novel.name == "祖魔"
            })
        }
    }

    @Test
    fun searchNovelAuthor() {
        context.getNovelList(context.searchNovelAuthor("一夜风云起").requester).let {
            it.forEach { novelItem ->
                println(novelItem)
            }
            assertTrue(it.any { novelItem ->
                novelItem.novel.name == "祖魔"
            })
        }
    }

    @Test
    fun getNovelDetail() {
        context.getNovelDetail(DetailRequester("https://www.snwx8.com/book/66/66076/")).let {
            assertEquals("祖魔", it.novel.name)
            assertEquals("一夜风云起", it.novel.author)
            assertEquals("祖魔的简介：一个超级世家大家族少爷，却天生丹田堵塞，难以修真！本有着疼爱自己的父母，却一夜之间家族没落，自此身负血海深仇！恋人的背叛，父母的离去，最终让他指天怒骂！天道不公，以万物为刍狗！既然天已无道，我愿舍身成魔！以血染天，以杀破道！", it.introduction)
            assertEquals("https://www.snwx8.com/files/article/image/66/66076/66076s.jpg", it.bigImg)
            println(it.update)
        }
        // TODO: 获取不到封面，麻烦，是图片有地址但下载不到，要加上onError,
        context.getNovelDetail(DetailRequester("https://www.snwx8.com/book/257/257710/")).let {
            assertEquals("都市超级神尊", it.novel.name)
            assertEquals("小萌靓", it.novel.author)
            assertEquals("https://www.snwx8.com/modules/article/images/nocover.jpg", it.bigImg)
            println(it.introduction)
            println(it.update)
        }
    }

    @Test
    fun getNovelChaptersAsc() {
        context.getNovelChaptersAsc(ChaptersRequester("https://www.snwx8.com/book/0/20/")).let { list ->
            list.forEach {
                println(it)
            }
            assertEquals("第一章 狠角色", list.first().name)
        }
    }

    @Test
    fun getNovelText() {
        context.getNovelText(TextRequester("https://www.snwx8.com/book/0/20/9888.html")).textList.let {
            assertEquals(40, it.size)
            assertEquals("(新书上传期间，求会员点击、推荐、收藏……拜谢！)", it.first())
            assertEquals("张卫东微微皱了皱眉头，却是没有回头，继续走他的路。", it.last())
        }
    }
}
