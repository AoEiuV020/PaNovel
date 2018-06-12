package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.base.jar.notNull
import cc.aoeiuv020.irondb.Iron
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File
import java.nio.charset.Charset

/**
 * Created by AoEiuV020 on 2018.06.12-17:27:05.
 */
class TextImporterTest {
    private val local = TextImporter(Iron.db(File("/tmp/panovel/test/local/importer")))

    /**
     * 本app导出的小说的导入测试，
     */
    @Test
    fun panovel() {
        javaClass.getResourceAsStream("/panovel.txt").notNull().use { input ->
            local.importText(input, Charset.forName("UTF-8"))
        }
        assertEquals("圣骑士的传说", local.author)
        assertEquals("修真聊天群", local.name)
        val introduction = local.introduction.notNull()
        assertEquals("某天，宋书航意外加入了一个仙侠中二病资深患者的交流群，里面的群友们都以‘道友’相称，群名片都是各种府主、洞主、真人、天师。连群主走失的宠物犬都称为大妖犬离家出走。整天聊的是炼丹、闯秘境、炼功经验啥的。", introduction.first())
        assertEquals("九洲一号群（VIP书友群，需验证）63769632", introduction.last())
        assertEquals(7, introduction.size)
        val chapters = local.chapters.notNull()
        assertEquals(8, chapters.size)
        chapters.first().let {
            assertEquals("有趣的书评同人小故事", it.name)
            val content = local.getContent(it.extra)
            assertEquals("感觉这些有趣的书评小故事被埋没下去好可惜，我会慢慢整理出来的。", content.first())
            assertEquals("——————————————————————————————", content.last())
            assertEquals(11, content.size)
        }
        chapters[4].let {
            assertEquals("第1929章 许多上了年份的东西，都是好东西", it.name)
            val content = local.getContent(it.extra)
            assertEquals(0, content.size)
        }
        chapters.last().let {
            assertEquals("第1991章 看来胖球大佬没留后手", it.name)
            val content = local.getContent(it.extra)
            assertEquals("“这可是我怼了九幽胖球大佬后的战利品呀。”宋书航感慨道。", content.first())
            assertEquals("胖球大佬弄出...", content.last())
            assertEquals(4, content.size)
        }
    }

    /**
     * 本app以前导出的小说的导入测试，
     */
    @Test
    fun panovelOld() {
        javaClass.getResourceAsStream("/panovel-old.txt").notNull().use { input ->
            local.importText(input, Charset.forName("UTF-8"))
        }
        assertNull(local.author)
        assertNull(local.name)
        assertNull(local.introduction)
        val chapters = local.chapters.notNull()
        assertEquals(22, chapters.size)
        chapters.first().let {
            assertEquals("有趣的书评同人小故事", it.name)
            val content = local.getContent(it.extra)
            assertEquals("感觉这些有趣的书评小故事被埋没下去好可惜，我会慢慢整理出来的。", content.first())
            assertEquals("——————————————————————————————", content.last())
            assertEquals(11, content.size)
        }
        chapters[2].let {
            assertEquals("《黄山大傻之歌》完整版", it.name)
            val content = local.getContent(it.extra)
            assertEquals(0, content.size)
        }
        chapters.last().let {
            assertEquals("第1989章 你们看，这是宋某的木头身躯", it.name)
            val content = local.getContent(it.extra)
            assertEquals("待天帝离开之后。", content.first())
            assertEquals("“我这只是在装个逼。”什么子回道：“我当时还在和天庭的长生者纠缠着...", content.last())
            assertEquals(3, content.size)
        }
    }

    /**
     * 知轩藏书的导入测试，
     */
    @Test
    fun zxcs() {
        javaClass.getResourceAsStream("/zxcs.txt").notNull().use { input ->
            local.importText(input, Charset.forName("GBK"))
        }
        assertEquals("卜非", local.author)
        assertEquals("与千年女鬼同居的日子", local.name)
        val introduction = local.introduction.notNull()
        assertEquals("为了赚点零花钱代人扫墓，结果一只女鬼跟着回了家，额滴个神呀，从此诡异的事情接二连三的发生在了自己身边。", introduction.first())
        assertEquals("虽然这只女鬼长得俊俏又漂亮，可等知道她的真正身份之后，我和我的小伙伴顿时都惊呆了。", introduction.last())
        assertEquals(4, introduction.size)
        val chapters = local.chapters.notNull()
        assertEquals(5, chapters.size)
        chapters.first().let {
            assertEquals("第一卷 红衣夜女", it.name)
            val content = local.getContent(it.extra)
            assertEquals(0, content.size)
        }
        chapters[1].let {
            assertEquals("第1章 女鬼来了", it.name)
            val content = local.getContent(it.extra)
            assertEquals("黄昏时分，燕京郊区，西山墓园边的一块规划坟地。", content.first())
            assertEquals("矮坟不大，上面的泥土还散发着一股新鲜的气息，像是最近才翻盖的一般。几根小草从矮坟中冒出了头。", content.last())
            assertEquals(6, content.size)
        }
        chapters.last().let {
            assertEquals("第82章 报仇", it.name)
            val content = local.getContent(it.extra)
            assertEquals("红衣女鬼终于被搞定了，可是，刘浪的心却再也无法平静了。", content.first())
            assertEquals("这阴阳书就是破除韩家诅咒的关键。", content.last())
            assertEquals(5, content.size)
        }
    }
}