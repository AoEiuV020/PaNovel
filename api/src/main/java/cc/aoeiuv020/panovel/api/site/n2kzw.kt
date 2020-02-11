package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.atry.tryOrNul
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstThreeIntPattern
import cc.aoeiuv020.panovel.api.firstTwoIntPattern
import cc.aoeiuv020.panovel.api.reverseRemoveDuplication
import cc.aoeiuv020.regex.compilePattern
import cc.aoeiuv020.string.divide
import org.jsoup.nodes.Element

/**
 * Created by AoEiuV020 on 2018.06.08-18:47:21.
 */
class N2kzw : DslJsoupNovelContext() {init {
    site {
        name = "2k中文网"
        baseUrl = "http://www.2kzw.com"
        logo = "http://www.2kzw.com/17mb/images/logo.png"
    }
    // 这网站所有名字都是/遮天(精校版)/官道无疆(校对版)/
    val pickName = { e: Element ->
        e.text().replace(compilePattern("\\(\\S+版\\)$").toRegex(), "")
    }
    search {
        get {
            // http://www.2kzw.com/modules/article/search.php?searchtype=articlename&searchkey=%B6%BC%CA%D0&page=1
            charset = "GBK"
            url = "/modules/article/search.php"
            data {
                "searchtype" to "articlename"
                "searchkey" to it
                // 加上&page=1可以避开搜索时间间隔的限制，
                // 也可以通过不加载cookies避开搜索时间间隔的限制，
                "page" to "1"
            }
        }
        document {
            single("^/\\d+/\\d+/") {
                name("#info > h1", block = pickName)
                author("#info > p:nth-child(2)", block = pickString("作\\s*者：(\\S*)"))
            }
            items("#nr") {
                name("> td:nth-child(1) > a", block = pickName)
                author("> td:nth-child(3)")
            }
        }
    }
    // http://www.2kzw.com/5/5798/
    bookIdRegex = firstTwoIntPattern
    detailPageTemplate = "/%s/"
    detail {
        document {
            novel {
                name("#info > h1", block = pickName)
                author("#info > p:nth-child(2)", block = pickString("作\\s*者：(\\S*)"))
            }
            // https://www.2kzw.com/files/article/image/3/3426/3426s.jpg
            tryOrNul {
                image = site.baseUrl + "/files/article/image/$it/${it.divide('/').second}s.jpg"
            }
            introduction("#intro > p")
        }
    }
    chapters {
        document {
            items("#list > dl > dd > a")
        }
                .reverseRemoveDuplication()
                // https://www.2kzw.com/0/852/
                // 开头的缓存章节莫名多了一个中间章节，两次去重可以去掉，
                .reverseRemoveDuplication()
    }
    // http://www.2kzw.com/5/5798/6586941.html
    bookIdWithChapterIdRegex = firstThreeIntPattern
    contentPageTemplate = "/%s.html"
    content {
        document {
            items("#content")
        }
    }
}
}

