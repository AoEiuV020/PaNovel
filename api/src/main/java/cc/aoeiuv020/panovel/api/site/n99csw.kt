package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.textList
import cc.aoeiuv020.encrypt.base64Decode
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.regex.compileRegex
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import java.net.URLEncoder
import java.nio.charset.Charset

@Suppress("DefaultLocale")
class N99csw : DslJsoupNovelContext() {init {
    site {
        name = "99藏书网"
        baseUrl = "https://99csw.com"
        logo = "text://99藏书网/?fc=df2029&bc=eee4c2"
    }
    hostList += "m.99csw.com"
    search {
        get {
            // https://m.99csw.com/book/search.list.php?keyword=%25E9%2583%25BD%25E5%25B8%2582&stat=true&page=1
            url = "//m.99csw.com/book/search.list.php"
            data {
                @Suppress("DEPRECATION")
                "keyword" to URLEncoder.encode(it)
                "stat" to "true"
                "page" to "1"
            }
        }
        document {
            items("book") {
                name = root.attr("name")
                extra = root.attr("id")
                author = root.attr("author")
            }
        }
    }
    // https://99csw.com/book/8430/index.htm
    bookIdRegex = "/book/(\\d+)/"
    detailPageTemplate = "/book/%s/index.htm"
    detail {
        document {
            novel {
                name("#book_info > h2")
                author("#book_info > h4:nth-child(4) > a")
            }
            image("#book_info > img")
            introduction("#book_info > div.intro")
        }
    }
    chapters {
        document {
            items("dl#dir > dd > a")
        }
    }
    // https://99csw.com/book/8430/297812.htm
    contentPageTemplate = "/book/%s.htm"
    content {
        document {
            // 原本是childNodes，但实际上没有textNode，所有node都是element,
            val boxChildNodes = root.requireElement("#content").children()
            val star = 1 + boxChildNodes.indexOfLast { it.tagName().toLowerCase() == "h2" }
            val e = root.ownerDocument().getElementsByTag("meta")[4].attr("content").base64Decode()
                .toString(Charset.defaultCharset()).split(compileRegex("[A-Z]+%"))
                .map { it.toInt() }
            e.max()
            val childNode = mutableMapOf<Int, Element>()
            var j = 0
            e.forEachIndexed { i, ei ->
                if (ei < 3) {
                    childNode[ei] = boxChildNodes[i + star]
                    j++
                } else {
                    childNode[ei - j] = boxChildNodes[i + star]
                    j += 2
                }
            }
            // TODO: 过滤网址广告，
            // <s>ｗww•９９ｌib.net</s>
            // <figure>99lib.net</figure>
            novelContent = childNode.toSortedMap().values.flatMap {
                it.textList()
            }
        }
    }
}
}

