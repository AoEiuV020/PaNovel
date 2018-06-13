package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.base.jar.notNull
import cc.aoeiuv020.panovel.data.entity.Novel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.charset.Charset

/**
 * Created by AoEiuV020 on 2018.06.13-18:02:42.
 */
class TextParserTest {
    init {
        System.setProperty("org.slf4j.simpleLogger.log.TextParser", "trace")
    }

    @Rule
    @JvmField
    val folder = TemporaryFolder()

    /**
     * 本app导出的小说的导入测试，
     */
    @Test
    fun panovel() {
        val file = folder.newFile()
        val charset = "UTF-8"
        val local = javaClass.getResourceAsStream("/panovel.txt").notNull("resource").use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
            TextParser(file, Charset.forName(charset))
                    .parse()
        }
        val expectedNovel = Novel(
                site = ".txt",
                author = "圣骑士的传说",
                name = "修真聊天群",
                detail = file.absoluteFile.canonicalPath,
                introduction = "某天，宋书航意外加入了一个仙侠中二病资深患者的交流群，里面的群友们都以‘道友’相称，群名片都是各种府主、洞主、真人、天师。连群主走失的宠物犬都称为大妖犬离家出走。整天聊的是炼丹、闯秘境、炼功经验啥的。\n" +
                        "突然有一天，潜水良久的他突然发现……群里每一个群员，竟然全部是修真者，能移山倒海、长生千年的那种！\n" +
                        "啊啊啊啊，世界观在一夜间彻底崩碎啦！\n" +
                        "书友群：九洲1号群:207572656\n" +
                        "九洲２号群:168114177\n" +
                        "九洲３号群:165210665（新）\n" +
                        "九洲一号群（VIP书友群，需验证）63769632",
                chapters = charset
        )
        val novel = Novel(
                site = local.type.suffix,
                author = local.author.notNull("author"),
                name = local.name.notNull("name"),
                detail = file.absoluteFile.canonicalPath,
                introduction = local.introduction.notNull("introduction"),
                chapters = charset
        )
        assertEquals(expectedNovel, novel)
        val chapters = local.chapters.notNull("chapters")
        assertEquals(8, chapters.size)
        val provider = TextProvider(novel)
        chapters.first().let {
            assertEquals("有趣的书评同人小故事", it.name)
            val content = provider.getNovelContent(it)
            assertEquals("感觉这些有趣的书评小故事被埋没下去好可惜，我会慢慢整理出来的。", content.first())
            assertEquals("——————————————————————————————", content.last())
            assertEquals(11, content.size)
        }
        chapters[4].let {
            assertEquals("第1929章 许多上了年份的东西，都是好东西", it.name)
            val content = provider.getNovelContent(it)
            assertEquals(0, content.size)
        }
        chapters.last().let {
            assertEquals("第1991章 看来胖球大佬没留后手", it.name)
            val content = provider.getNovelContent(it)
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
        val file = folder.newFile()
        val charset = "UTF-8"
        val local = javaClass.getResourceAsStream("/panovel-old.txt").notNull("resource").use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
            TextParser(file, Charset.forName(charset))
                    .parse()
        }
        assertNull(local.author)
        assertNull(local.name)
        assertNull(local.introduction)
        val novel = Novel(
                site = local.type.suffix,
                author = "author",
                name = "name",
                detail = file.absoluteFile.canonicalPath,
                introduction = "introduction",
                chapters = charset
        )
        val provider = TextProvider(novel)
        val chapters = local.chapters.notNull()
        assertEquals(22, chapters.size)
        chapters.first().let {
            assertEquals("有趣的书评同人小故事", it.name)
            val content = provider.getNovelContent(it)
            assertEquals("感觉这些有趣的书评小故事被埋没下去好可惜，我会慢慢整理出来的。", content.first())
            assertEquals("——————————————————————————————", content.last())
            assertEquals(11, content.size)
        }
        chapters[2].let {
            assertEquals("《黄山大傻之歌》完整版", it.name)
            val content = provider.getNovelContent(it)
            assertEquals(0, content.size)
        }
        chapters.last().let {
            assertEquals("第1989章 你们看，这是宋某的木头身躯", it.name)
            val content = provider.getNovelContent(it)
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
        val file = folder.newFile()
        val charset = "GBK"
        val local = javaClass.getResourceAsStream("/zxcs.txt").notNull("resource").use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
            TextParser(file, Charset.forName(charset))
                    .parse()
        }
        val expectedNovel = Novel(
                site = ".txt",
                author = "卜非",
                name = "与千年女鬼同居的日子",
                detail = file.absoluteFile.canonicalPath,
                introduction = "为了赚点零花钱代人扫墓，结果一只女鬼跟着回了家，额滴个神呀，从此诡异的事情接二连三的发生在了自己身边。\n" +
                        "红衣夜女杀人案、枯井中的无脸之人、河中的人形怪物……\n" +
                        "更为奇怪的是，那些平时连想都不敢想的女神都主动凑了过来。\n" +
                        "虽然这只女鬼长得俊俏又漂亮，可等知道她的真正身份之后，我和我的小伙伴顿时都惊呆了。",
                chapters = charset
        )
        val novel = Novel(
                site = local.type.suffix,
                author = local.author.notNull("author"),
                name = local.name.notNull("name"),
                detail = file.absoluteFile.canonicalPath,
                introduction = local.introduction.notNull("introduction"),
                chapters = charset
        )
        assertEquals(expectedNovel, novel)
        val provider = TextProvider(novel)
        val chapters = local.chapters.notNull()
        assertEquals(5, chapters.size)
        chapters.first().let {
            assertEquals("第一卷 红衣夜女", it.name)
            val content = provider.getNovelContent(it)
            assertEquals(0, content.size)
        }
        chapters[1].let {
            assertEquals("第1章 女鬼来了", it.name)
            val content = provider.getNovelContent(it)
            assertEquals("黄昏时分，燕京郊区，西山墓园边的一块规划坟地。", content.first())
            assertEquals("矮坟不大，上面的泥土还散发着一股新鲜的气息，像是最近才翻盖的一般。几根小草从矮坟中冒出了头。", content.last())
            assertEquals(6, content.size)
        }
        chapters.last().let {
            assertEquals("第82章 报仇", it.name)
            val content = provider.getNovelContent(it)
            assertEquals("红衣女鬼终于被搞定了，可是，刘浪的心却再也无法平静了。", content.first())
            assertEquals("这阴阳书就是破除韩家诅咒的关键。", content.last())
            assertEquals(5, content.size)
        }
    }
}