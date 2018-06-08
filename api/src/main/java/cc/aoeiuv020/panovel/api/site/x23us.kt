package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstIntPattern
import cc.aoeiuv020.panovel.api.firstThreeIntPattern

/**
 * Created by AoEiuV020 on 2018.06.08-18:01:41.
 */
class X23us : DslJsoupNovelContext() {init {
    site {
        name = "顶点小说"
        baseUrl = "https://www.x23us.com"
        logo = "https://www.x23us.com/themes/xiaoshuo/logo.gif"
    }
    search {
        get {
            // https://www.x23us.com/modules/article/search.php?searchtype=keywords&searchkey=%B6%BC%CA%D0
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
                name("#content > dd:nth-child(2) > h1", block = pickString("(\\S+) 全文阅读"))
                author("#at > tbody > tr:nth-child(1) > td:nth-child(4)")
            }
            items("#content > table > tbody > tr:not(:nth-child(1))") {
                name("> td:nth-child(1) > a")
                author("> td:nth-child(3)")
            }
        }
    }
    // https://www.x23us.com/book/57703
    bookIdRegex = firstIntPattern
    detailPageTemplate = "/book/%s"
    detail {
        document {
            novel {
                name("#content > dd:nth-child(2) > h1", block = pickString("(\\S+) 全文阅读"))
                author("#at > tbody > tr:nth-child(1) > td:nth-child(4)")
            }
            image("#content > dd:nth-child(3) > div:nth-child(1) > a > img")
            update("#at > tbody > tr:nth-child(2) > td:nth-child(6)", format = "yyyy-MM-dd")
            introduction("#content > dd:nth-child(7) > p:nth-child(3)")
        }
    }
    // https://www.x23us.com/html/57/57703/
    chapterDivision = 1000
    chaptersPageTemplate = "/html/%d/%s/"
    chapters {
        document {
            items("#at > tbody > tr > td > a")
            lastUpdate("#a_main > div.bdsub > dl > dd:nth-child(4) > h3", format = "yyyy-MM-dd", block = pickString("更新时间：(.*)"))
        }
    }
    // https://www.x23us.com/html/57/57703/23689132.html
    bookIdWithChapterIdRegex = firstThreeIntPattern
    contentPageTemplate = "/html/%s.html"
    content {
        document {
            items("#contents")
        }
    }
}
}

