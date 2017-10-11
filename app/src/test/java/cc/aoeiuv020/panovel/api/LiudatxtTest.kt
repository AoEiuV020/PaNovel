package cc.aoeiuv020.panovel.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 *
 * Created by AoEiuV020 on 2017.10.11-21:00:18.
 */
class LiudatxtTest {
    init {
        System.setProperty("org.slf4j.simpleLogger.log.Liudatxt", "trace")
    }

    private lateinit var context: Liudatxt
    @Before
    fun setUp() {
        context = Liudatxt()
    }

    @Test
    fun getGenres() {
        val genres = context.getGenres()
        genres[0].let {
            assertEquals("玄幻奇幻", it.name)
            assertEquals("http://www.liudatxt.com/mulu/1.html", it.requester.url)
        }
        genres[genres.size - 1].let {
            assertEquals("女频频道", it.name)
            assertEquals("http://www.liudatxt.com/mulu/7.html", it.requester.url)
        }
    }

    @Test
    fun getNextPage() {
        context.getNextPage(NovelGenre("", "http://www.liudatxt.com/mulu/1-2.html")).let {
            assertEquals("http://www.liudatxt.com/mulu/1-3.html", it!!.requester.url)
        }
    }

    @Test
    fun getNovelList() {
        context.getNovelList(ListRequester("http://www.liudatxt.com/mulu/1.html")).let {
            it.forEach { novelItem ->
                println(novelItem)
            }
            assertEquals(20, it.size)
        }
    }

    @Test
    fun searchNovelName() {
        context.getNovelList(context.searchNovelName("诸天万界反派聊天群").requester).let {
            it.forEach { novelItem ->
                println(novelItem)
            }
            assertTrue(it.any { novelItem ->
                novelItem.novel.name == "诸天万界反派聊天群"
            })
        }
    }

    @Test
    fun searchNovelAuthor() {
        context.getNovelList(context.searchNovelAuthor("不要尬舞").requester).let {
            it.forEach { novelItem ->
                println(novelItem)
            }
            assertTrue(it.any { novelItem ->
                novelItem.novel.name == "诸天万界反派聊天群"
            })
        }
    }

    @Test
    fun getNovelDetail() {
        context.getNovelDetail(DetailRequester("http://www.liudatxt.com/so/22921/")).let {
            assertEquals("http://www.liudatxt.com/headimgs/22/22921/s22921.jpg", it.bigImg)
            assertEquals("诸天万界反派聊天群", it.novel.name)
            assertEquals("不要尬舞", it.novel.author)
            assertEquals("科幻灵异", it.genre)
            assertEquals("这个穿越画风有些不对？开局就是地狱难度是个什么鬼？还好自带一个金手指。 " +
                    "从此以后踏上了诸天万界各大反派人生导师的不归路！露出一脸和善微笑的雄霸正指挥着风云怒肛帝释天、" +
                    "海贼世界已经成为一个出色海军的路飞正带着自己的海军攻打四皇、" +
                    "以及把令狐冲当做亲儿子一般的对待满脸正气的岳不群。 " +
                    "......刘锋叹了一...", it.info)
            println(it.update)
            println(it.status)
            println(it.stars)
            println(it.length)
            println(it.lastChapter)
        }
    }

    @Test
    fun getNovelChaptersAsc() {
        context.getNovelChaptersAsc(ChaptersRequester("http://www.liudatxt.com/so/22921/")).let { list ->
            list.forEach {
                println(it)
            }
            assertEquals("第1章 穿越到噩梦难度的世界该怎么办？", list.first().name)
        }
    }

    @Test
    fun getNovelText() {
        context.getNovelText(TextRequester("http://www.liudatxt.com/so/22921/8710426.html")).textList.let {
            assertEquals(37, it.size)
            assertEquals("“你是怪物吗？！”这会儿刘锋是真的被面前这个身上血迹斑斑的哥们给惊着了，卧槽，二十多刀刀刀避开要害，简直是人才！", it.first())
            assertEquals("顺带一提，为了避免实力一样引起的怀疑，刘锋稍稍的将刘淼这个账号的实力水平降低了一点设定在二级。", it.last())
        }
    }
}