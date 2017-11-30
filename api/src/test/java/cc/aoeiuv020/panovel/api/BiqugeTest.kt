package cc.aoeiuv020.panovel.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 *
 * Created by AoEiuV020 on 2017.10.08-21:52:04.
 */
class BiqugeTest {
    init {
        System.setProperty("org.slf4j.simpleLogger.log.Biquge", "trace")
    }

    private lateinit var context: Biquge
    @Before
    fun setUp() {
        context = Biquge()
    }

    @Test
    fun getGenres() {
        val genres = context.getGenres()
        genres[0].let {
            assertEquals("玄幻小说", it.name)
            assertEquals("https://www.biqubao.com/xuanhuan/", it.requester.url)
        }
        genres[genres.size - 1].let {
            assertEquals("全本小说", it.name)
            assertEquals("https://www.biqubao.com/quanben/", it.requester.url)
        }
    }

    @Test
    fun getNextPage() {
        context.getNextPage(context.searchNovelName("最大权限")).let {
            assertTrue(it!!.requester.url.contains("p=1"))
        }
    }

    @Test
    fun getNovelList() {
        context.getNovelList(GenreListRequester("http://www.biqubao.com/xuanhuan/")).let {
            it.forEach { novelItem ->
                println(novelItem)
            }
            assertEquals(66, it.size)
        }
        context.getNovelList(GenreListRequester("http://www.biqubao.com/quanben/")).let {
            it.forEach { novelItem ->
                println(novelItem)
            }
            assertEquals(100, it.size)
        }
    }

    @Test
    fun searchNovelName() {
        context.getNovelList(context.searchNovelName("最大权限").requester).let {
            it.forEach { novelItem ->
                println(novelItem)
            }
            assertTrue(it.any { novelItem ->
                novelItem.novel.name == "最大权限"
            })
        }
    }

    @Test
    fun searchNovelAuthor() {
        context.getNovelList(context.searchNovelAuthor("肥鱼马甲").requester).let {
            it.forEach { novelItem ->
                println(novelItem)
            }
            assertTrue(it.any { novelItem ->
                novelItem.novel.name == "最大权限"
            })
        }
    }

    @Test
    fun getNovelDetail() {
        context.getNovelDetail(DetailRequester("http://www.biquge.cn/book/18156/")).let {
            assertEquals("最大权限", it.novel.name)
            assertEquals("肥鱼马甲", it.novel.author)
            assertEquals("制作游戏成功的林陨意外猝死，穿越到自己制作的游戏世界，结果拥有了这个世界的最大权限！\n" +
                    "最大权限书友群：305908807\n" +
                    "各位书友要是觉得《最大权限》还不错的话请不要忘记向您QQ群和微博里的朋友推荐哦！", it.introduction)
            println(it.bigImg)
            println(it.update)
        }
    }

    @Test
    fun getNovelChaptersAsc() {
        context.getNovelChaptersAsc(ChaptersRequester("http://www.biqubao.com/book/18156/")).let { list ->
            list.forEach {
                println(it)
            }
            assertEquals("序章", list.first().name)
        }
    }

    @Test
    fun getNovelText() {
        context.getNovelText(TextRequester("http://www.biqubao.com/book/18156/8791124.html")).textList.let {
            assertEquals(38, it.size)
            assertEquals("中年男子下意识的接住魂晶，这东西能够让卡片使更好的修炼魂力，可以说是硬通货，属于最高等的金钱。", it.first())
            assertEquals("“没什么，绿色级别吗，这不是问题。”林陨摇了摇头，接着拿出一张卡片，直接激发，同时林陨的气息也迅速增强，瞬间魂力翻倍，直接突破一千，迈过绿色级别的门槛。", it.last())
        }
    }
}