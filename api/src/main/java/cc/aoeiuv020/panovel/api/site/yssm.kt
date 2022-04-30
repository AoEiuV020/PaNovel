package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.anull.notNull
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstThreeIntPattern
import cc.aoeiuv020.panovel.api.firstTwoIntPattern
import cc.aoeiuv020.panovel.api.reverseRemoveDuplication
import org.jsoup.Jsoup
import java.io.ByteArrayOutputStream

/**
 * Created by AoEiuV020 on 2018.05.10-16:48:32.
 */
class Yssm : DslJsoupNovelContext() {init {
    reason = "有用户也上不去，看来不是我的网站问题，我这里同一个教育网的电脑能上手机不能上，"
    upkeep = false
    site {
        name = "幼狮书盟"
        baseUrl = "https://www.yssm.tv/"
        logo = "https://www.yssm.tv//images/logo.png"
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
        val max = 20 * 1000
        val response = response(call.notNull())
        val byteArrayOutputStream = ByteArrayOutputStream(max)
        response.inputStream {
            it.copyTo(byteArrayOutputStream, max)
        }
        val cutDocument = Jsoup.parse(byteArrayOutputStream.toByteArray().inputStream(), null, response.request().url().toString())
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
                author("> div > span > em", parent = div, block = pickString("作\\s*者：(\\S*)"))
            }
            // 这网站小说没有封面，
            image = null
            introduction("> p.intro", parent = div, block = ownLinesString())
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

