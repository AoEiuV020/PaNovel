package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.reverseRemoveDuplication

/**
 * Created by AoEiuV020 on 2018.06.03-19:35:33.
 */
class Lread : DslJsoupNovelContext() {init {
    site {
        name = "乐阅读"
        baseUrl = "https://www.6ks.net"
        logo = "https://www.6ks.net/images/logo.gif"
    }
    search {
        post {
            // https://www.lread.net/modules/article/search.php?searchkey=%B6%BC%CA%D0
            charset = "UTF-8"
            url = "/search.php"
            data {
                "searchtype" to "articlename"
                "searchkey" to it
            }
        }
        // 搜索结果可能过多，但是页面不太大，无所谓了，
        document {
            items("#nr") {
                name("> td:nth-child(1) > a")
                author("> td:nth-child(3)")
            }
        }
    }
    // https://m.lread.net/book/88917.html
    // https://www.lread.net/read/88917/
    detailPageTemplate = "/read/%s/"
    detail {
        document {
            novel {
                name("#info > h1")
                author("#info > p:nth-child(2)", block = pickString("作\\s*者：(\\S*)"))
            }
            image("#fmimg > img")
            update("#info > p:nth-child(4)", format = "yyyy-MM-dd HH:mm:ss", block = pickString("最后更新：(.*)"))
            introduction("#intro > p")
        }
    }
    chapters {
        document {
            items("#list > dl > dd > a")
            lastUpdate("#info > p:nth-child(4)", format = "yyyy-MM-dd HH:mm:ss", block = pickString("最后更新：(.*)"))
        }.reverseRemoveDuplication()
    }
    // https://www.lread.net/read/88917/32771268.html
    contentPageTemplate = "/read/%s.html"
    content {
        document {
            items("#content")
        }
    }
}
}

