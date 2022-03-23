package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import java.net.URL

class Wukong0 : DslJsoupNovelContext() {init {
    hide = true
    site {
        name = "悟空看书0"
        baseUrl = "https://www.wkxs.net"
        logo = "https://imgsa.baidu.com/forum/w%3D580/sign=95ca1b6f75310a55c424defc87474387/930dadd12f2eb938f9acfb6ed9628535e7dd6f50.jpg"
    }
    search {
        get {
            // 电脑版搜索要验证码，
            // https://m.wkxs.net/modules/article/search.php?searchtype=articlename&searchkey=%B6%BC%CA%D0&t_btnsearch=
            charset = "GBK"
            url = "//m.wkxs.net/modules/article/search.php"
            data {
                "searchtype" to "articlename"
                "searchkey" to it
                // 加上&page=1可以避开搜索时间间隔的限制，
                // 也可以通过不加载cookies避开搜索时间间隔的限制，
                "page" to "1"
            }
        }
        document {
            if (URL(root.ownerDocument().location()).path.startsWith("/book_")) {
                single {
                    name("tbody > tr > td.info > h1") {
                        it.ownText()
                    }
                    author("tbody > tr > td.info > p:nth-child(2) > a")
                }
            } else {
                items("body > div.waps_r > table > tbody > tr") {
                    name("> td:nth-child(2) > div > a:nth-child(1)")
                    author("> td:nth-child(2) > div > p > span.mr15", block = pickString("作者:(\\S*)"))
                }
            }
        }
    }
    // https://www.wkxs.net/book_885/
    bookIdRegex = "/book_(\\d+)"
    detailPageTemplate = "/book_%s/"
    detail {
        document {
            novel {
                name("#info > h1") {
                    it.ownText()
                }
                author("#info > h1 > small > a")
            }
            image("#picbox > div > img")
            update("#info > div.update", format = "yyyy-MM-dd HH:mm", block = pickString(".*（([^（]*)）"))
            introduction("#intro", block = ownLinesString())
        }
    }
    chapters {
        document {
            items("body > div.zjbox > dl > dd > a")
            lastUpdate("#info > div.update", format = "yyyy-MM-dd HH:mm", block = pickString(".*（([^（]*)）"))
        }
    }
    bookIdWithChapterIdRegex = "/book_(\\d+/\\d+)"
    contentPageTemplate = "/book_%s.html"
    content {
        document {
            items("#content", block = ownLinesSplitWhitespace())
        }.dropWhile {
            it.startsWith("悟空看书")
                    || it.startsWith("www.wkxs.net")
                    || it.startsWith("www.wukong.la")
                    || it == "最新章节！"
        }.dropLastWhile {
            it == "-->>"
        }
    }
}
}

