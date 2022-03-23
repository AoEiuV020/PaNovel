@file:Suppress("UnnecessaryVariable")

package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext

class N9txs : DslJsoupNovelContext() {init {
    // 服务器超时，可能是碰巧炸了，
//    enabled = false
    site {
        name = "九桃小说"
        baseUrl = "https://www.9taoxs.com"
        logo =
            "https://tiebapic.baidu.com/forum/pic/item/810a19d8bc3eb135716cd7dfb11ea8d3fd1f442e.jpg"
    }
    search {
        post {
            // https://www.9txs.com/search.html?searchkey=%E9%83%BD%E5%B8%82
            url = "https://so.9txs.org/www/"
            data {
                "searchkey" to it
            }
        }
        document {
            items("ul.library > li") {
                name("> a.bookname")
                author("a.author")
            }
        }
    }
    // https://www.9txs.com/book/43776.html
    detailPageTemplate = "/book/%s.html"
    detail {
        document {
            novel {
                name("div.detail > h1")
                author("div.detail > p:nth-child(3) > a:nth-child(1)")
            }
            image("div.detail > a > img")
            update("div.detail > p:nth-child(6) > span", format = "yyyy-MM-dd HH:mm:ss", block = pickString("\\((.*)\\)"))
            introduction("p.intro")
        }
    }
    // https://www.9txs.com/book/43776/
    chaptersPageTemplate = "/book/%s/"
    chapters {
        document {
            items("div.read > dl:not(:contains(最新章节)) > dd > a")
            lastUpdate("div.headline > p:nth-child(4)", format = "yyyy-MM-dd HH:mm", block = pickString("更新：(.*)"))
        }
    }
    // https://www.9txs.com/book/43776/166315.html
    contentPageTemplate = "/book/%s.html"
    content {
        document {
            items("#content")
        }.dropWhile {
            it.startsWith("九桃小说")
        }.dropLastWhile {
            it.startsWith("您可以在百度里搜索")
        }
    }
}
}

