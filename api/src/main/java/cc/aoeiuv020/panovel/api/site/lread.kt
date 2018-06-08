package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext

/**
 * Created by AoEiuV020 on 2018.06.03-19:35:33.
 */
class Lread : DslJsoupNovelContext() {init {
    site {
        name = "乐阅读"
        baseUrl = "https://www.lread.net"
        logo = "https://www.lread.net/images/logo.gif"
    }
    search {
        post {
            // https://www.lread.net/modules/article/search.php?searchkey=%B6%BC%CA%D0
            // 电脑版搜索不可用，手机版可用，
            // https://m.lread.net/s.php
            // s=%B6%BC%CA%D0&type=articlename
            charset = "GBK"
            url = "//m.lread.net/s.php"
            data {
                "type" to "articlename"
                "s" to it
            }
        }
        // 搜索结果可能过多，但是页面不太大，无所谓了，
        document {
            items("body > div.search_box > p.search_list") {
                name("> a:nth-child(1)")
                author("> a:nth-child(3)")
            }
        }
    }
    // https://www.lread.net/read/88917/
    detailPageTemplate = "/read/%s/"
    detail {
        document {
            novel {
                name("#info > h1")
                author("#info > p:nth-child(2) > a:nth-child(1)")
            }
            image("#fmimg > img")
            update("#info > p:nth-child(4)", format = "yyyy年MM月dd日HH:mm", block = pickString("更新时间：(.*)"))
            introduction("#intro > p:nth-child(2)")
        }
    }
    chapters {
        document {
            items("#list > dl > dd > a")
            lastUpdate("#info > p:nth-child(4)", format = "yyyy年MM月dd日HH:mm", block = pickString("更新时间：(.*)"))
        }
    }
    // https://www.lread.net/read/88917/32771268.html
    contentPageTemplate = "/read/%s.html"
    content {
        document {
            items("#booktext", block = ownLinesSplitWhitespace())
        }
    }
}
}

