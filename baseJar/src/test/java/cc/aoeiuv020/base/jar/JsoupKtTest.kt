package cc.aoeiuv020.base.jar

import org.jsoup.Jsoup
import org.junit.Test

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
}