package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext

/**
 * Created by AoEiuV020 on 2018.06.02-19:47:41.
 */
// 和Biquge完全一样，
class Byzw : DslJsoupNovelContext() {init {
    site {
        name = "八一中文网"
        baseUrl = "https://www.zwdu.com"
        logo = "https://www.zwdu.com/images/book_logo.png"
    }
    search {
        get {
            url = "/search.php"
            data {
                "keyword" to it
            }
        }
        document {
            items("div.result-list > div") {
                name("> div.result-game-item-detail > h3 > a")
                author("> div.result-game-item-detail > div > p:nth-child(1) > span:nth-child(2)")
            }
        }
    }
    detailPageTemplate = "/book/%s/"
    detail {
        document {
            novel {
                name("#info > h1")
                author("#info > p:nth-child(2)", block = pickString("作    者：(\\S*)"))
            }
            image("#fmimg > img")
            update("#info > p:nth-child(4)", format = "yyyy-MM-dd HH:mm:ss", block = pickString("最后更新：(.*)"))
            introduction("#intro > p:not(:nth-last-child(1))")
        }
    }
    chapters {
        document {
            items("#list > dl > dd > a")
            lastUpdate("#info > p:nth-child(4)", format = "yyyy-MM-dd HH:mm:ss", block = pickString("最后更新：(.*)"))
        }
    }
    contentPageTemplate = "/book/%s.html"
    content {
        document {
            items("#content")
        }
    }
}
}

