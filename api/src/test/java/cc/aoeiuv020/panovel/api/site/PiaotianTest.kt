package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.pick
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
    fun searchNovelName() {
        context.searchNovelName("斗破苍穹").let {
            it.forEach { novelItem ->
                println(novelItem)
            }
            assertTrue(it.any { novelItem ->
                novelItem.novel.name == "斗破苍穹"
            })
        }
        // 有小说搜索后直接跳到详情页，
        context.searchNovelName("从前").let {
            it.forEach { novelItem ->
                println(novelItem)
            }
            assertTrue(it.any { novelItem ->
                novelItem.novel.name == "从前有座灵剑山"
            })
        }
    }

    @Test
    fun getNovelDetail() {
        context.getNovelDetail("https://www.piaotian.com/bookinfo/8/8605.html").let {
            assertEquals("剑灵同居日记", it.novel.name)
            assertEquals("国王陛下", it.novel.author)
            assertEquals("“天外神剑剑灵，应呼唤而苏醒，我问你，你就是我的坐骑么？”\n一个无敌的随身剑灵与天才美少女（们）的同居故事。", it.introduction)
            println(it.bigImg)
            println(it.update)
        }
    }

    @Test
    fun getNovelChapters() {
        context.getNovelChaptersAsc("https://www.piaotian.com/html/4/4316/index.html").let { list ->
            list.forEach {
                println(it)
            }
            assertEquals("序幕：天外飞仙+第一章：客栈柴房温暖如春", list.first().name)
        }
        context.getNovelChaptersAsc("https://www.piaotian.com/html/8/8912/index.html").let { list ->
            list.forEach {
                println(it)
            }
            assertEquals("第001章 狂暴系统", list.first().name)
            assertTrue(list.last().name.isNotEmpty())
        }
    }

    @Test
    fun getNovelText() {
        context.getNovelText("https://www.piaotian.com/html/8/8605/5582838.html").textList.let {
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
        val pattern = "" +
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
        val list = novelDetail.pick(pattern)
        println(list)
    }
}
