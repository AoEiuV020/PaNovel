package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.notNull
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstThreeIntPattern
import cc.aoeiuv020.panovel.api.firstTwoIntPattern
import cc.aoeiuv020.panovel.api.noImage
import cc.aoeiuv020.panovel.api.reverseRemoveDuplication
import org.jsoup.Jsoup

/**
 * Created by AoEiuV020 on 2018.05.10-16:48:32.
 */
class Yssm : DslJsoupNovelContext() {init {
    site {
        name = "幼狮书盟"
        baseUrl = "https://www.yssm.org"
        logo = "https://www.yssm.org/images/logo.png"
    }
    search {
        get {
            url = "/SearchBook.php"
            data {
                "keyword" to it
            }
        }
        // 傻哔吧这网站，一次性返回所有，搜索都市直接出四千多结果，html大于1M，
        // 这里限制一下，20K大概十几个结果，
        val byteArray = ByteArray(20 * 1000)
        val response = response(call.notNull())
        response.inputStream { it.read(byteArray) }
        val cutDocument = Jsoup.parse(byteArray.inputStream(), null, response.request().url().toString())
        document(cutDocument) {
            // 由于被截断，可能处理最后一个元素会出异常，无视，
            itemsIgnoreFailed("#container > div.details.list-type > ul > li") {
                name("> span.s2 > a")
                author("> span.s3")
            }
        }
    }
    bookIdRegex = firstTwoIntPattern
    // https://www.yssm.org/uctxt/227/227934/
    detailPageTemplate = "/uctxt/%s/"
    detail {
        document {
            val div = root.requireElement("#container > div.bookinfo")
            novel {
                name("> div > span > h1", parent = div)
                author("> div > span > em", parent = div, block = pickString("作者：(\\S*)"))
            }
            // 这网站小说没有封面，
            image = noImage
            introduction("> p.intro", parent = div) {
                it.ownTextList().joinToString("\n")
            }
            update("> p.stats > span.fr > i:nth-child(2)", parent = div, format = "yyyy/MM/dd HH:mm:ss")
        }
    }
    chapters {
        document {
            items("#main > div > dl > dd > a")
        }.reverseRemoveDuplication()
    }
    bookIdWithChapterIdRegex = firstThreeIntPattern
    // https://www.yssm.org/uctxt/227/227934/1301112.html
    contentPageTemplate = "/uctxt/%s.html"
    content {
        document {
            items("#content")
        }
    }
}
}

