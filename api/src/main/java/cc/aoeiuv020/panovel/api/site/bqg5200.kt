package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstThreeIntPattern

/**
 * Created by AoEiuV020 on 2018.06.06-18:51:02.
 */
class Bqg5200 : DslJsoupNovelContext() {init {
    // 网页测试大面积报红，
    // http://tool.chinaz.com/speedtest/https://www.bqg5200.com
    enabled = false
    site {
        name = "笔趣阁5200"
        baseUrl = "https://www.bqg5200.com"
        logo = "https://www.bqg5200.com/skin/images/logo.png"
    }
    search {
        get {
            // https://www.bqg5200.com/modules/article/search.php?searchtype=articlename&searchkey=%B6%BC%CA%D0&page=1
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
            single("^/book/") {
                name("#bookinfo > div.bookright > div.booktitle > h1")
                author("#author", block = pickString("作\\s*者：(\\S+)"))
            }
            items("#conn > table > tbody > tr:not(:nth-child(1))") {
                name("> td:nth-child(1) > a")
                author("> td:nth-child(3)")
            }
        }
    }
    // https://www.bqg5200.com/book/2889/
    bookIdRegex = "/(book|xiaoshuo/\\d+)/(\\d+)"
    bookIdIndex = 1
    detailPageTemplate = "/book/%s/"
    detail {
        document {
            novel {
                name("#bookinfo > div.bookright > div.booktitle > h1")
                author("#author", block = pickString("作\\s*者：(\\S+)"))
            }
            image("#bookimg > img")
            update("#bookinfo > div.bookright > div.new > span.new_t", format = "最后更新：yyyy-MM-dd")
            introduction("#bookintro > p", block = ownLinesString())
        }
    }
    // https://www.bqg5200.com/xiaoshuo/2/2889/
    chapterDivision = 1000
    chaptersPageTemplate = "/xiaoshuo/%d/%s/"
    chapters {
        document {
            items("#readerlist > ul > li > a")
            lastUpdate("#smallcons > span:nth-child(4)", format = "yyyy-MM-dd HH:mm")
        }
    }
    // https://www.bqg5200.com/xiaoshuo/3/3590/11768349.html
    bookIdWithChapterIdRegex = firstThreeIntPattern
    contentPageTemplate = "/xiaoshuo/%s.html"
    content {
        document {
            items("#content", block = ownLinesSplitWhitespace())
        }
    }
}
}

