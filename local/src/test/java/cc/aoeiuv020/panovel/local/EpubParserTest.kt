package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.base.jar.absSrc
import cc.aoeiuv020.base.jar.absXlinkHref
import cc.aoeiuv020.base.jar.pick
import cc.aoeiuv020.base.jar.textList
import net.sf.jazzlib.ZipFile
import nl.siegmann.epublib.epub.EpubReader
import org.jsoup.Jsoup
import org.junit.Assert.*
import org.junit.Test
import java.net.URL
import java.nio.charset.Charset

/**
 * Created by AoEiuV020 on 2018.06.16-17:52:10.
 */
class EpubParserTest : ParserTest(EpubParser::class) {
    @Test
    fun yidm() {
        val file = getFile("/home/aoeiuv/tmp/panovel/epub/yidm/Re：从零开始的异世界生活_第十一卷.epub") ?: return
        val charset = "UTF-8"
        val parser = EpubParser(file, Charset.forName(charset))
        val chapters = chapters(
                parser,
                author = null,
                name = "Re：从零开始的异世界生活-第十一卷-迷糊动漫",
                requester = charset,
                image = "OEBPS/Images/Cover.jpg",
                introduction = null
        )
        assertEquals(12, chapters.size)
        chapters.first().let {
            assertEquals("封面", it.name)
            val content = parser.getNovelContent(it.extra)
            assertEquals("![img](jar:${file.toURI()}!/OEBPS/Images/Cover.jpg)", content.first())
            assertEquals(content.first(), content.last())
            assertEquals(1, content.size)
        }
        chapters[1].let {
            assertEquals("书名", it.name)
            val content = parser.getNovelContent(it.extra)
            assertEquals("Re：从零开始的异世界生活", content.first())
            assertEquals("插画: 大塚真一郎", content.last())
            assertEquals(4, content.size)
        }
        chapters.last().let {
            assertEquals("第十一卷 后记", it.name)
            val content = parser.getNovelContent(it.extra)
            assertEquals("后记", content.first())
            assertEquals("![img](jar:${file.toURI()}!/OEBPS/Images/97172.jpg)", content.last())
            assertEquals(24, content.size)
        }
    }

    @Test
    fun lightNovel() {
        val file = getFile("/home/aoeiuv/tmp/panovel/epub/打工吧！魔王大人17.epub") ?: return
        val charset = "UTF-8"
        val parser = EpubParser(file, Charset.forName(charset))
        val chapters = chapters(
                parser,
                author = "和ヶ原 聡司",
                name = "[和ヶ原聡司].打工吧！魔王大人17",
                requester = charset,
                image = "cover1.jpeg",
                introduction = "魔王大人，在正式职员录用考试中落选！\n" +
                        "之后由于木崎的调动命令，职员们乱成一团！！\n" +
                        "众望所归的广播剧也预定在2017年6月7日发售！！！\n" +
                        "魔王在正式职员录用考试中落选，十分不甘，比以往更加努力工作，展现出神级职员的一面。然而，失去了从异世界安特·伊苏拉流落日本以来一直努力的目标，他的外表与内心都十分消沉。\n" +
                        "并且，遭到袭击、保持在可爱小鸡的柔弱状态的卡米奥，还有“大魔王的遗产”中最后一件“星际宝石”的所在地，必须解决的问题堆积如山。\n" +
                        "其中，有人在代代木周边目击到被认为握有卡米奥遇袭关键的存在。与天祢共赴现场的魔王撞见了鳄鱼般的恶魔。好不容易确保的、需要照料的三坪房间VILLA ROSA笹塚201号房间，无论在工作中还是私人事务中，魔王的心都无暇休息……\n" +
                        "麦丹劳一侧，由于店长木崎接到调动命令，以惠美和千穗为首，职员们广受动摇。木崎为商量未来事项而面见魔王，惠美和千穗察觉魔王已经没有留在日本的理由，开始感到不安。是回到安特·伊苏拉，还是留在日本继续工作，魔王开始为今后的事业该如何是好而烦恼，他做出的选择是——\n" +
                        "为您献上工作成分很多的庶民派FANTASY第17卷！"
        )
        assertEquals(13, chapters.size)
        chapters.first().let {
            assertEquals("封面", it.name)
            val content = parser.getNovelContent(it.extra)
            assertEquals("![img](jar:${file.toURI()}!/cover1.jpeg)", content.first())
            assertEquals(content.first(), content.last())
            assertEquals(1, content.size)
        }
        chapters[1].let {
            assertEquals("制作信息", it.name)
            val content = parser.getNovelContent(it.extra)
            assertEquals("![img](jar:${file.toURI()}!/OPS/images/17-007.jpg)", content.first())
            assertEquals("------------------------------------------------------------------------", content.last())
            assertEquals(11, content.size)
        }
        chapters.last().let {
            assertEquals("作者，后记 ——AND YOU——", it.name)
            val content = parser.getNovelContent(it.extra)
            assertEquals(it.name, content.first())
            assertEquals("那就再会啦！", content.last())
            assertEquals(18, content.size)
        }
    }

    @Test
    fun ixdzs() {
        // https://www.ixdzs.com/d/64/64839/
        val file = getFile("/home/aoeiuv/tmp/panovel/epub/大圣传.epub") ?: return
        val charset = "UTF-8"
        val parser = EpubParser(file, Charset.forName(charset))
        val chapters = chapters(
                parser,
                author = "说梦者",
                name = "大圣传",
                requester = charset,
                image = "cover.jpg",
                introduction = "大圣传\n" +
                        "妖魔中的至高无上者，名为“大圣”。"
        )
        // 包括封面，
        assertEquals(1653, chapters.size)
        chapters.first().let {
            assertEquals("书籍信息", it.name)
            val content = parser.getNovelContent(it.extra)
            assertEquals("大圣传", content.first())
            assertEquals("『手机请访问:m.ixdzs.com』", content.last())
            assertEquals(8, content.size)
        }
        chapters[1].let {
            assertEquals("第一章 青牛开口", it.name)
            val content = parser.getNovelContent(it.extra)
            assertEquals(it.name, content.first())
            assertEquals("书迷楼最快更新，无弹窗阅读请收藏书迷楼(.com)。", content.last())
            assertEquals(133, content.size)
        }
        chapters.last().let {
            assertEquals("第十四章 愿望", it.name)
            val content = parser.getNovelContent(it.extra)
            assertEquals(it.name, content.first())
            assertEquals("========================================", content.last())
            assertEquals(70, content.size)
        }
    }

    @Test
    fun regex() {
        val intro = "『内容简介：妖魔中的至高无上者，名为“大圣”。\n" +
                "     』"
        val res = intro.pick("内容简介：([^』]*)(』)?").first().trim()
        println(res)
    }

    @Test
    fun url() {
        val file = getFile("/home/aoeiuv/tmp/panovel/epub/打工吧！魔王大人17.epub") ?: return
        listOf(
                "file:$file",
                "file://$file",
                "file://localhost$file"
        ).forEach {
            // file协议三种数量的斜杆/都能支持，
            // 默认URL.toString是一个斜杆/的，
            assertEquals(file.toURI().toURL(), URL(it))
        }
        // jar协议最后的斜杆/是必须的，拼接的时候不能重复，
        val url = URL("jar:${file.toURI()}!/")
        val coverUrl = URL(url, "cover1.jpeg")
        assertEquals("jar:file:$file!/cover1.jpeg", coverUrl.toString())
        // URL能正确处理jar协议的根路径，
        assertEquals(coverUrl, URL(url, "/cover1.jpeg"))
        assertNull(coverUrl.authority)
        assertEquals("", coverUrl.host)
        // path带文件路径真不爽，总觉得有矛盾，转到根目录还要包括path,
        assertEquals("file:$file!/cover1.jpeg", coverUrl.path)
        assertEquals(coverUrl.path, coverUrl.file)
        // 如果文件不存在，openString会抛FileNotFoundException，
        coverUrl.openStream().use { input ->
            // 图片第一个字符，
            assertEquals(0xff, input.read())
        }

        val image = Jsoup.parse("""
            <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" version="1.1" width="100%" height="100%" viewBox="0 0 546 751" preserveAspectRatio="none">
                <image width="100%" height="100%" xlink:href="cover1.jpeg"/>
            </svg>
            """, "jar:${file.toURI()}!/a/s/d")
                .select("image").first()
        // Jsoup可以正确处理jar协议地址，
        assertEquals("jar:${file.toURI()}!/a/s/cover1.jpeg", image.absXlinkHref())

        val imageParent = Jsoup.parse("""
                <img src="../Image/Cover.jpg"/>
            """, "jar:${file.toURI()}!/a/s/d")
                .select("img").first()
        // Jsoup不能正确处理上级目录的决定路径，
        assertEquals("jar:/${file.toURI()}!/a/Image/Cover.jpg", imageParent.attr("abs:src"))
        // 自己实现封装URL处理，
        assertEquals("jar:${file.toURI()}!/a/Image/Cover.jpg", imageParent.absSrc())

        val httpUrl = URL(coverUrl, "http://example.com/a/s/d")
        // 改协议也正常，
        assertEquals("http://example.com/a/s/d", httpUrl.toString())

        val parentUrl = URL(URL(coverUrl, "a/s"), "../../../Images/aa.jpg")
        // 能正确处理上一级，
        assertEquals("jar:${file.toURI()}!/Images/aa.jpg", parentUrl.toString())

    }

    @Test
    fun yidmTest() {
        val file = getFile("/home/aoeiuv/tmp/panovel/epub/yidm/Re：从零开始的异世界生活_第十一卷.epub") ?: return
        val charset = "UTF-8"
        val zipFile = ZipFile(file)
        val book = EpubReader().readEpubLazy(zipFile, charset)

        // epublib3.1不支持从cdata中拿文本，自己fork的改改，应该是要支持的，
        assertEquals("Re：从零开始的异世界生活-第十一卷-迷糊动漫", book.metadata.titles.single())
    }

    @Test
    fun epublibTest() {
        val file = getFile("/home/aoeiuv/tmp/panovel/epub/打工吧！魔王大人17.epub") ?: return
        val charset = "UTF-8"
        val zipFile = ZipFile(file)
        // 就算是lazy，也会先加载不少东西，考虑用的时候by lazy,
        // zip文件不带编码，一样要自己指定，
        // readEpub时就已经用传入的编码解析了META-INF/container.xml得到的记录所有信息的content.opf,
        val book = EpubReader().readEpubLazy(zipFile, charset)

        val spines = book.spine.spineReferences
        assertEquals(13, spines.size)
        // spine记录在content.opf中，不带章节名，
        assertTrue(spines.all { it.resource.title == null })

        val contents = book.contents
        assertEquals(13, contents.size)
        assertTrue(contents.all { it.title == null })


        // tableOfContents只拿content.opf中记录的章节，不一定完整，爱下电子书的epub这个目录没有包括封面，
        book.tableOfContents.let {
            assertEquals(13, it.size())
            assertEquals(1, it.calculateDepth())
            it.tocReferences.let {
                // 这本小说没有二级目录，
                assertTrue(it.all { it.children.size == 0 })
                it.first().let {
                    assertEquals("封面", it.title)
                    assertEquals("titlepage.xhtml", it.resource.href)
                    // 有fragmentId的情况这两个会不同，但不知道什么情况会有fragmentId，
                    assertEquals(it.resource.href, it.completeHref)
                }
                it[9].let {
                    assertEquals("勇者，透露对未来的不安", it.title)
                    assertEquals("OPS/chapter9.html", it.completeHref)
                }
                it.last().let {
                    assertEquals("作者，后记 ——AND YOU——", it.title)
                    assertEquals("OPS/chapter12.html", it.completeHref)
                }
            }
        }

        book.coverPage.let {
            // 封面也是一个单独页面，一般只包含一张图片，
            assertEquals("titlepage.xhtml", it.href)
        }

        book.opfResource.let {
            assertEquals("content.opf", it.href)
        }

        book.coverImage.let {
            assertEquals("cover1.jpeg", it.href)
            // 图片第一个字符，
            assertEquals(0xff.toByte(), it.data.first())
        }

        assertEquals("[和ヶ原聡司].打工吧！魔王大人17", book.title)

        val author = book.metadata.authors.first().run { "$firstname $lastname" }

        assertEquals("和ヶ原 聡司", author)

        val intro = book.metadata.descriptions.flatMap {
            Jsoup.parse(it).body()
                    .textList()
        }.joinToString("\n")
        assertEquals("魔王大人，在正式职员录用考试中落选！\n" +
                "之后由于木崎的调动命令，职员们乱成一团！！\n" +
                "众望所归的广播剧也预定在2017年6月7日发售！！！\n" +
                "魔王在正式职员录用考试中落选，十分不甘，比以往更加努力工作，展现出神级职员的一面。然而，失去了从异世界安特·伊苏拉流落日本以来一直努力的目标，他的外表与内心都十分消沉。\n" +
                "并且，遭到袭击、保持在可爱小鸡的柔弱状态的卡米奥，还有“大魔王的遗产”中最后一件“星际宝石”的所在地，必须解决的问题堆积如山。\n" +
                "其中，有人在代代木周边目击到被认为握有卡米奥遇袭关键的存在。与天祢共赴现场的魔王撞见了鳄鱼般的恶魔。好不容易确保的、需要照料的三坪房间VILLA ROSA笹塚201号房间，无论在工作中还是私人事务中，魔王的心都无暇休息……\n" +
                "麦丹劳一侧，由于店长木崎接到调动命令，以惠美和千穗为首，职员们广受动摇。木崎为商量未来事项而面见魔王，惠美和千穗察觉魔王已经没有留在日本的理由，开始感到不安。是回到安特·伊苏拉，还是留在日本继续工作，魔王开始为今后的事业该如何是好而烦恼，他做出的选择是——\n" +
                "为您献上工作成分很多的庶民派FANTASY第17卷！", intro)
    }
}