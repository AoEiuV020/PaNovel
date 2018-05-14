package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.ChaptersRequester
import cc.aoeiuv020.panovel.api.DetailRequester
import cc.aoeiuv020.panovel.api.GenreListRequester
import cc.aoeiuv020.panovel.api.TextRequester
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 *
 * Created by AoEiuV020 on 2018.03.06-19:25:09.
 */
class SfacgTest {
    init {
        System.setProperty("org.slf4j.simpleLogger.log.Sfacg", "trace")
    }

    private lateinit var context: Sfacg
    @Before
    fun setUp() {
        context = Sfacg()
    }

    @Test
    fun getGenres() {
        val genres = context.getGenres()
        genres[0].let {
            assertEquals("全部", it.name)
            assertEquals("http://book.sfacg.com/List/", it.requester.url)
        }
        genres[genres.size - 1].let {
            assertEquals("社会类", it.name)
            assertEquals("http://book.sfacg.com/List/?tid=15", it.requester.url)
        }
    }

    @Test
    fun getNextPage() {
        context.getNextPage(context.getGenres()[1]).let {
            assertEquals("http://book.sfacg.com/List/default.aspx?tid=1&PageIndex=2", it!!.requester.url)
        }
    }

    @Test
    fun getNovelList() {
        context.getNovelList(GenreListRequester("http://book.sfacg.com/List/?tid=4")).let {
            it.forEach { novelItem ->
                println(novelItem)
            }
            assertEquals(20, it.size)
        }
        context.getNovelList(GenreListRequester("http://book.sfacg.com/List/default.aspx?tid=1&PageIndex=2")).let {
            it.forEach { novelItem ->
                println(novelItem)
            }
            assertEquals(20, it.size)
        }
    }

    @Test
    fun searchNovelName() {
        context.getNovelList(context.searchNovelName("黑猫变成少女才不奇怪呢").requester).let {
            it.forEach { novelItem ->
                println(novelItem)
            }
            assertTrue(it.any { novelItem ->
                novelItem.novel.name == "黑猫变成少女才不奇怪呢"
            })
        }
    }

    @Test
    fun searchNovelAuthor() {
        context.getNovelList(context.searchNovelAuthor("青衣流苏").requester).let {
            it.forEach { novelItem ->
                println(novelItem)
            }
            assertTrue(it.any { novelItem ->
                novelItem.novel.name == "黑猫变成少女才不奇怪呢"
            })
        }
    }

    @Test
    fun getNovelDetail() {
        context.getNovelDetail(DetailRequester("http://book.sfacg.com/Novel/123589")).let {
            assertEquals("黑猫变成少女才不奇怪呢", it.novel.name)
            assertEquals("青衣流苏", it.novel.author)
            assertEquals("好久好久，都没有这么快乐过了。黑发的少女如彼端如此叹息。\n" +
                    "“你的世界，真美。”", it.introduction)
            assertEquals("http://rs.sfacg.com/web/novel/images/NovelCover/Big/2018/02/556aa0b6-25b5-43de-bd60-9a7bc9234ba5.jpg", it.bigImg)
            println(it.update)
        }
        context.getNovelDetail(DetailRequester("http://book.sfacg.com/Novel/114367/")).let {
            assertEquals("我的学生们都是病娇大小姐", it.novel.name)
            assertEquals("诡话连篇", it.novel.author)
            assertEquals("“同学们，下课了！”我站在讲台上整理了一下衣服，怯怯地说道：“所以你们能把我的手铐打开吗……”\n" +
                    "我的可爱学生们一齐答道：“啊咧，这怎么可以呢？合格的老师就应该二十四小时陪伴在学生身边吧？”\n" +
                    "什么？我拿你们当学生，你们却想上我？！\n" +
                    "还有坐在后面的同学，请务必放下你手中的柴..", it.introduction)
            assertEquals("http://rs.sfacg.com/web/novel/images/NovelCover/Big/2018/03/cef45df7-b012-460b-a18b-85f4bb4be97b.jpg", it.bigImg)
            println(it.update)
        }
    }

    @Test
    fun getNovelChaptersAsc() {
        context.getNovelChaptersAsc(ChaptersRequester("http://book.sfacg.com/Novel/114367/MainIndex/")).let { list ->
            list.forEach {
                println(it)
            }
            assertEquals("二月月票福利计划~", list.first().name)
        }
    }

    @Test
    fun getNovelText() {
        context.getNovelText(TextRequester("http://book.sfacg.com/Novel/114367/191449/1584685/")).textList.let {
            assertEquals(34, it.size)
            assertEquals("樱才高中，位于K市市中心，是一所名副其实的贵族女校。", it.first())
            assertEquals("“喂，等等，你们要什么？不要拉我……我真的没有语言猥亵学生啊！”", it.last())
        }
        context.getNovelText(TextRequester("http://book.sfacg.com/vip/c/1725750/")).textList.let {
            assertEquals(1, it.size)
            assertEquals("双手抱胸的短发女孩，浑身上下都透露着浓厚的“夹击妹抖”（日语“风纪委员”谐音）气息，简洁干练的学生制服，不施粉黛略带怒容的俏颜，如果再给她配上一根教鞭，一定会成为令所有学生闻风丧胆的存在。 但我可不..", it.first())
        }
    }
}