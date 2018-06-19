package cc.aoeiuv020.base.jar

import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.net.URL

/**
 * Created by AoEiuV020 on 2018.06.18-18:51:38.
 */
class JsoupKtTest {
    @Test
    fun p() {
        val html = """<p>a</p><p>b</p>"""
        val root = Jsoup.parse(html)
        root.body().textList().forEach {
            println(it)
        }
    }

    @Test
    fun span() {
        val html = """<p><span>a</span><span>b</span>a</p>"""
        val root = Jsoup.parse(html)
        root.body().textList().forEach {
            println(it)
        }
        root.body().text().let { println(it) }
    }

    @Test
    fun parentUrl() {
        val file = File("/home/aoeiuv/tmp/panovel/epub/yidm/Re：从零开始的异世界生活_第十一卷.epub")
                .toURI()
        assertEquals("file:/home/aoeiuv/tmp/panovel/epub/yidm/Re：从零开始的异世界生活_第十一卷.epub", file.toString())
        val url = "jar:$file!/OEBPS/Text/CoverPage.xhtml"
        val html = """<img src="../Image/Cover.jpg"></img>"""
        val root = Jsoup.parse(html, url)
        root.select("img").forEach {
            assertEquals("../Image/Cover.jpg", it.attr("src"))
            // jsoup解析jar协议上一级会出现多余的斜杆/, 和它自己额外的处理有关，
            assertEquals("jar:/$file!/OEBPS/Image/Cover.jpg", it.attr("abs:src"))
            assertEquals("jar:$file!/OEBPS/Image/Cover.jpg", URL(URL(url), "../Image/Cover.jpg").toString())
        }
        assertEquals("![img](jar:$file!/OEBPS/Image/Cover.jpg)", root.body().textList().single())
    }
}