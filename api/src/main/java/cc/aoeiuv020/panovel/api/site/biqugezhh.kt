@file:Suppress("UnnecessaryVariable")

package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.reverseRemoveDuplication

class Biqugezhh : DslJsoupNovelContext() {init {
    reason = "搜索失效了，搜出来的会是另一个网站的结果，www.qu-la.com"
    upkeep = false
    site {
        name = "笔趣阁zhh"
        baseUrl = "https://www.zhhbiqu.com"
        logo = "https://www.zhhbiqu.com/images/logo.png"
    }
    // https://so.biqusoso.com/s.php?ie=utf-8&siteid=zanghaihuatxt.com&q=%E5%85%B5%E7%8E%8B
    hostList += "so.biqusoso.com"
    search {
        get {
            url = "//so.biqusoso.com/s.php"
            data {
                "ie" to "utf-8"
                "siteid" to "zanghaihuatxt.com"
                "q" to it
            }
        }
        document {
            items("div.search-list > ul > li:not(:nth-child(1))") {
                name("> span.s2 > a")
                author("> span.s4")
            }
        }
    }
    // https://www.biquzhh.com/80451_80451829/
    // http://www.zanghaihuatxt.com/book/goto/id/80451829
    bookIdRegex = "_?(\\d+)"
    detailDivision = 1000
    chapterDivision = detailDivision
    detailPageTemplate = "/%d_%s/"
    detail {
        document {
            novel {
                name("#info > h1")
                author("#info > p:nth-child(2)", block = pickString("作\\s*者：(\\S*)"))
            }
            image("#fmimg > img")
            update("#info > p:nth-child(5)", format = "最后更新：yyyy-MM-dd HH:mm:ss")
            introduction("#intro > p:nth-child(1)")
        }
    }
    chapters {
        document {
            items(".listmain > dl > dd > a")
            lastUpdate("#info > p:nth-child(5)", format = "最后更新：yyyy-MM-dd HH:mm:ss")
        }.reverseRemoveDuplication()
    }
    bookIdWithChapterIdRegex = "/(\\d+_\\d+/\\d+)"
    contentPageTemplate = "/%s.html"
    content {
        document {
            items("#content", block = ownLines())
        }.dropLastWhile { it.contains("biquzhh") }
            .dropLastWhile { it.contains("zhhbiqu") }
    }
}
}

