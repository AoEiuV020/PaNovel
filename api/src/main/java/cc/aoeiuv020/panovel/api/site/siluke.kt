package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.ownTextListSplitWhitespace
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstIntPattern
import cc.aoeiuv020.panovel.api.firstTwoIntPattern
import java.net.URL

/**
 * Created by AoEiuV020 on 2018.06.05-18:24:49.
 */
class Siluke : DslJsoupNovelContext() {init {
    site {
        name = "思路客"
        baseUrl = "http://www.siluke.org"
        logo = "http://www.siluke.org/templates/images/logo.png"
    }
    search {
        get {
            // http://www.siluke.org/modules/article/search.php?searchtype=articlename&searchkey=%B6%BC%CA%D0&page=1
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
            if (URL(root.ownerDocument().location()).path.startsWith("/book/")) {
                single {
                    name("#content > dd:nth-child(1) > h1")
                    author("#at > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(4) > a:nth-child(1)")
                }
            } else {
                items(".grid > tbody:nth-child(2) > tr:not(:nth-child(1))") {
                    name("> td:nth-child(1) > a:nth-child(1)")
                    author("> td:nth-child(3)")
                }
            }
        }
    }
    // http://www.siluke.org/book/68917/
    bookIdRegex = firstIntPattern
    detailPageTemplate = "/book/%s/"
    detail {
        document {
            novel {
                name("#content > dd:nth-child(1) > h1")
                author("#at > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(4) > a:nth-child(1)")
            }
            image(".hst > img:nth-child(1)")
            update("#at > tbody > tr:nth-child(2) > td:nth-child(6)", format = "yyyy-MM-dd")
            introduction(".intro")
        }
    }
    // http://www.siluke.org/book/68917/index.html
    chaptersPageTemplate = "/book/%s/index.html"
    chapters {
        document {
            items("#at > tbody:nth-child(1) > tr > td > a")
        }
    }
    // http://www.siluke.org/book/68917/20835244.html
    bookIdWithChapterIdRegex = firstTwoIntPattern
    contentPageTemplate = "/book/%s.html"
    content {
        document {
            items("#contents") {
                it.textNodes().drop(1).flatMap { it.ownTextListSplitWhitespace() }
            }
        }
    }
}
}

