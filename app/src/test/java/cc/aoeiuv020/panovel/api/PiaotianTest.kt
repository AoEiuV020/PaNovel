package cc.aoeiuv020.panovel.api

import org.jsoup.Jsoup
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 *
 * Created by AoEiuV020 on 2017.10.02-16:07:05.
 */
class PiaotianTest {
    init {
        System.setProperty("org.slf4j.simpleLogger.log.Piaotian", "trace")
    }

    private lateinit var context: Piaotian
    @Before
    fun setUp() {
        context = Piaotian()
    }

    @Test
    fun getGenres() {
        val genres = context.getGenres()
        genres[0].let {
            assertEquals("玄幻魔法", it.name)
            assertEquals("http://www.piaotian.com/booksort1/0/1.html", it.url)
        }
        genres[genres.size - 1].let {
            assertEquals("全本小说", it.name)
            assertEquals("http://www.piaotian.com/quanben/index.html", it.url)
        }
    }

    @Test
    fun getNextPage() {
        context.getNextPage(NovelGenre("玄幻魔法", "http://www.piaotian.com/booksort1/0/1.html")).let {
            assertEquals("http://www.piaotian.com/booksort1/0/2.html", it!!.url)
        }
        context.getNextPage(NovelGenre("玄幻魔法", "http://www.piaotian.com/booksort1/0/2.html")).let {
            assertEquals("http://www.piaotian.com/booksort1/0/3.html", it!!.url)
        }
        context.getNextPage(NovelGenre("玄幻魔法", "http://www.piaotian.com/booksort1/0/83.html")).let {
            assertNull(it)
        }
        context.getNextPage(NovelGenre("全本小说", "http://www.piaotian.com/quanben/index.html")).let {
            assertEquals("http://www.piaotian.com/quanben/index.html?page=2", it!!.url)
        }
    }

    @Test
    fun getNovelList() {
        context.getNovelList(NovelGenre("玄幻魔法", "http://www.piaotian.com/booksort1/0/1.html")).let {
            it.forEach { novelItem ->
                println(novelItem)
            }
            assertEquals(30, it.size)
        }
        context.getNovelList(context.searchNovelName("斗破苍穹")).let {
            it.forEach { novelItem ->
                println(novelItem)
            }
        }
        context.getNovelList(context.searchNovelAuthor("国王")).let {
            it.forEach { novelItem ->
                println(novelItem)
            }
        }
    }

    @Test
    fun searchNovelName() {
    }

    @Test
    fun searchNovelAuthor() {
    }

    @Test
    fun isSearchResult() {
        assertTrue(context.isSearchResult(NovelGenre("", "http://www.piaotian.com/modules/article/search.php")))
        assertFalse(context.isSearchResult(NovelGenre("", "http://www.piaotian.com/booksort1/0/1.html")))
    }

    @Test
    fun getNovelDetail() {
        context.getNovelDetail(NovelDetailUrl("http://www.piaotian.com/bookinfo/8/8605.html")).let {
            assertEquals("剑灵同居日记", it.novel.name)
            assertEquals("国王陛下", it.novel.author)
            assertEquals("武侠修真", it.genre)
            assertEquals("“天外神剑剑灵，应呼唤而苏醒，我问你，你就是我的坐骑么？”\n一个无敌的随身剑灵与天才美少女（们）的同居故事。", it.info)
            println(it.bigImg)
            println(it.length)
            println(it.update)
            println(it.status)
            println(it.stars)
            println(it.chaptersAsc.size)
            println(it.chaptersAsc.first())
            println(it.chaptersAsc.last())
        }
    }

    @Test
    fun getNovelText() {
        context.getNovelText(NovelChapter("", "http://www.piaotian.com/html/8/8605/5582838.html")).textList.let {
            assertEquals(21, it.size)
            assertEquals("6月1日凌晨0点，本书正式上架。", it.first())
            assertEquals("请各位绅士们量力而行，不必强求逆天。", it.last())
        }
    }

    @Test
    fun regexText() {
        val novelDetail = """剑灵同居日记
类    别：武侠修真	作    者：国王陛下	管 理 员：	全文长度：1041337字
最后更新：2017-10-01	文章状态：连载中	授权级别：暂未授权	首发状态：他站首发
收 藏 数：168	总推荐数：300	本月推荐：1	收到鲜花：1"""
        val pattern ="" +
                "(\\S*)\\s" +
                "类    别：(\\S*)\\s" +
                "作    者：(\\S*)\\s" +
                "管 理 员：(\\S*)\\s" +
                "全文长度：(\\S*)\\s" +
                "最后更新：(\\S*)\\s" +
                "文章状态：(\\S*)\\s" +
                "授权级别：(\\S*)\\s" +
                "首发状态：(\\S*)\\s" +
                "收 藏 数：(\\S*)\\s" +
                "总推荐数：(\\S*)\\s" +
                "本月推荐：(\\S*)\\s" +
                "收到鲜花：(\\S*)" +
                ""
        val list = novelDetail.pick(pattern)!!
        println(list)
    }

    @Test
    fun pickInfo() {
        val html = """<div style="float:left;width:390px;">
          <span class="hottext">最新章节：</span><a href="http://www.piaotian.com/html/8/8605/5903637.html">第51章 真不愧是轻茗的亲姨</a><br>

		  <br>
          <span class="hottext">内容简介：</span><br>&nbsp;&nbsp;&nbsp;&nbsp;“天外神剑剑灵，应呼唤而苏醒，我问你，你就是我的坐骑么？”<br>
&nbsp;&nbsp;&nbsp;&nbsp;一个无敌的随身剑灵与天才美少女（们）的同居故事。<br>
<br><br>

      </div>"""
        val d = Jsoup.parse(html)
        val div = d.select("div").first()
        div.text()
        val textList = div.textList()
        assertEquals("“天外神剑剑灵，应呼唤而苏醒，我问你，你就是我的坐骑么？”", textList[0])
        assertEquals("一个无敌的随身剑灵与天才美少女（们）的同居故事。", textList[1])
    }
}