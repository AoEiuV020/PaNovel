package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.compilePattern
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstThreeIntPattern
import cc.aoeiuv020.panovel.api.firstTwoIntPattern
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
                name("body > div.main.w > div.articleinfo > div.r > div.l2 > div.p1 > h1", block = pickName)
                author("body > div.main.w > div.articleinfo > div.r > div.l2 > div.p1 > span > a")
            }
            items("body > div.main.w > ul > li") {
                name("> p.d1 > a", block = pickName)
                author("> p.d2 > span.author > a")
            }
        }
    }
    // http://www.2kzw.com/5/5798/
    bookIdRegex = firstTwoIntPattern
    detailPageTemplate = "/%s/"
    detail {
        document {
            novel {
                name("body > div.main.w > div.articleinfo > div.r > div.l2 > div.p1 > h1", block = pickName)
                author("body > div.main.w > div.articleinfo > div.r > div.l2 > div.p1 > span > a")
            }
            image("body > div.main.w > div.articleinfo > div.l > p > img")
            introduction("body > div.main.w > div.articleinfo > div.r > div.l2 > p")
        }
    }
    chapters {
        document {
            items("body > div.main.w > div.chapterlist > div.read-list > ul > li > a")
        }
    }
    // http://www.2kzw.com/5/5798/6586941.html
    bookIdWithChapterIdRegex = firstThreeIntPattern
    contentPageTemplate = "/%s.html"
    content {
        // 第一段固定是章节名，
        document {
            items("#content > p")
        }.drop(1)
    }
}
}

