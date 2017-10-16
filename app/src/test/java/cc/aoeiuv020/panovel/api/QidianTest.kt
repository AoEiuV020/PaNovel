package cc.aoeiuv020.panovel.api

import org.jsoup.Jsoup
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 *
 * Created by AoEiuV020 on 2017.10.16-17:47:33.
 */
class QidianTest {
    init {
        System.setProperty("org.slf4j.simpleLogger.log.Qidian", "trace")
    }

    private lateinit var context: Qidian
    @Before
    fun setUp() {
        context = Qidian()
    }

//    @Test
    @Suppress("unused")
    fun makeGenres() {
        val all = Jsoup.connect("https://www.qidian.com/all").get()
        val chanIdList = all.select("div.work-filter.type-filter > ul > li").map {
            it.attr("data-id").toInt()
        }
        val map = chanIdList.joinToString(",\n") { chanId ->
            val root = Jsoup.connect("https://www.qidian.com/all?chanId=$chanId&orderId=&page=1&style=1&pageSize=20&siteid=1&hiddenField=0").get()
            val sub = root.select("div.sub-type > dl:not(.hidden) > dd").joinToString(",\n") {
                val name = it.text()
                val subCateId = it.attr("data-subtype").toInt()
                """"$name" to $subCateId"""
            }
            """$chanId to linkedMapOf($sub)"""
        }
        println("linkedMapOf($map)")
    }

    @Test

    fun getGenres() {
        context.getGenres().forEach {
            println(it)
        }
    }

    @Test
    fun getNextPage() {
        context.getNextPage(NovelGenre("东方玄幻", "https://www.qidian.com/all?chanId=21&subCateId=8&orderId=&page=1&style=1&pageSize=20&siteid=1&hiddenField=0")).let {
            Assert.assertEquals("https://www.qidian.com/all?chanId=21&subCateId=8&orderId=&style=1&pageSize=20&siteid=1&hiddenField=0&page=2", it!!.requester.url)
        }
    }

    @Test
    fun getNovelList() {
        context.getNovelList(GenreListRequester("https://www.qidian.com/all?chanId=21&subCateId=8&orderId=&page=1&style=1&pageSize=20&siteid=1&hiddenField=0")).let {
            it.forEach { novelItem ->
                println(novelItem)
            }
            Assert.assertEquals(20, it.size)
        }
    }

    @Test
    fun searchNovelName() {
        context.getNovelList(context.searchNovelName("我是女皇的随身铠甲").requester).let {
            it.forEach { novelItem ->
                println(novelItem)
            }
            Assert.assertTrue(it.any { novelItem ->
                novelItem.novel.name == "我是女皇的随身铠甲"
            })
        }
    }

    @Test
    fun searchNovelAuthor() {
        context.getNovelList(context.searchNovelAuthor("不要尬舞").requester).let {
            it.forEach { novelItem ->
                println(novelItem)
            }
            Assert.assertTrue(it.any { novelItem ->
                novelItem.novel.name == "诸天万界反派聊天群"
            })
        }
    }

    @Test
    fun getNovelDetail() {
        context.getNovelDetail(DetailRequester("https://book.qidian.com/info/1010436534")).let {
            Assert.assertEquals("https://qidian.qpic.cn/qdbimg/349573/1010436534/180", it.bigImg)
            Assert.assertEquals("我是女皇的随身铠甲", it.novel.name)
            Assert.assertEquals("一文倒", it.novel.author)
            Assert.assertEquals("东方玄幻", it.genre)
            Assert.assertEquals("盖世修为和《周天全书》，张夜带着他们重生，却成为了冰雪美人的专属甲胄，过上了给她当老爷爷的日子。\n" +
                    "“什么？有大boss？，丫头给我撞死他！”\n" +
                    "“高手？丫头给我撕了他！”\n" +
                    "“六臂食人猿？丫头，给我活吞了她！”\n" +
                    "“皇宫秘宝？偷？抢就完事了！”\n" +
                    "“丫头，你最近是不是又发育了，感觉有点绷啊...”\n" +
                    "铠甲在身，敌我不分！生死看淡，不服就干！\n" +
                    "这个故事讲的是，张夜如何培养出一位史上最生猛的女皇。", it.introduction)
            println(it.update)
            println(it.status)
            println(it.stars)
            println(it.length)
            println(it.lastChapter)
        }
    }

    @Test
    fun getNovelChaptersAsc() {
        context.getNovelChaptersAsc(ChaptersRequester("https://book.qidian.com/info/1010436534")).let { list ->
            list.forEach {
                println(it)
            }
            Assert.assertEquals("第一章 我叫张夜，是一颗陨石", list.first().name)
        }
    }

    @Test
    fun getNovelText() {
        context.getNovelText(TextRequester("https://read.qidian.com/chapter/XljeBHNXyTjoTMoHyHZuUA2/LlljjtD5ydO2uJcMpdsVgA2")).textList.let {
            Assert.assertEquals(71, it.size)
            Assert.assertEquals("矿井区。", it.first())
            Assert.assertEquals("那冒泡的灵根潭面上，竟然悠悠浮起了半颗光头...", it.last())
        }
    }
}