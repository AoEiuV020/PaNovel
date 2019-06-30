package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.href
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.regex.pick

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
            // keyword=%E9%83%BD%E5%B8%82&t=1
            charset = "UTF-8"
            url = "//m.lread.net/s.php"
            data {
                "t" to "1"
                "keyword" to it
            }
        }
        // 搜索结果可能过多，但是页面不太大，无所谓了，
        document {
            items("div[class^=hot_sale]") {
                extra("> p.title > a") { a ->
                    a.href().pick("_(\\d*)").first()
                }
                name("> p.title > a")
                author("> p.author > a")
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
            items("#booktext")
        }
    }
}
}

