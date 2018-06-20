package cc.aoeiuv020.panovel.local

import org.jsoup.nodes.Document
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.net.URL
import java.nio.charset.Charset

/**
 * Created by AoEiuV020 on 2018.06.19-22:57:24.
 */
class EpubExporterTest : ParserTest(EpubParser::class) {

    @Test
    fun yidm() {
        val file = getFile("/home/aoeiuv/tmp/panovel/epub/yidm/Re：从零开始的异世界生活_第十一卷.epub") ?: return
        val charset = "UTF-8"
        val tmpParser = EpubParser(file, Charset.forName(charset))
        val info = tmpParser.parse()
        val tmpFile = File("/tmp/fff.epub")
        var isDone = false
        var lastP = -1
        EpubExporter(tmpFile).export(info, tmpParser) { current, total ->
            if (current == total) {
                // 以防万一，多一个isDone判断避免结束会调顺序出问题时不能通知结束，
                isDone = true
            }
            // 进度分成一百份，
            val max = 100
            val progress = (current.toFloat() / total * max).toInt()
            if (progress > lastP && !isDone) {
                lastP = progress
                println("exporting $current/$total")
            }
            if (isDone) {
                println("exported")
            }
        }
        val parser = EpubParser(tmpFile, Charset.forName(info.requester))
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
    fun jsoup() {
        val file = getFile("/home/aoeiuv/tmp/panovel/epub/yidm/Re：从零开始的异世界生活_第十一卷.epub") ?: return
        val rootUrl = URL("jar:${file.toURI()}!/")
        val url = URL(rootUrl, "a/s")
        val root = Document.createShell(url.toString())
        val div = root.body().appendElement("div")
        div.appendElement("p")
                .text("TEXT")
        println(root.outerHtml())
    }
}