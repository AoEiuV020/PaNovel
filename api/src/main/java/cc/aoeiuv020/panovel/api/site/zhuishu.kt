package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext

/**
 * Created by AoEiuV020 on 2018.06.08-18:30:24.
 */
class Zhuishu : DslJsoupNovelContext() {init {
    site {
        name = "追书网"
        baseUrl = "https://www.zhuishu.tw"
        logo = "https://www.zhuishu.tw/images/logo.gif"
    }
    search {
        get {
            // https://www.zhuishu.tw/search.aspx?keyword=%E9%83%BD%E5%B8%82
            url = "/search.aspx"
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
    // https://www.zhuishu.tw/id58054/
    bookIdRegex = "/id(\\d+)"
    detailPageTemplate = "/id%s/"
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
    // https://www.zhuishu.tw/id58054/200787.html
    bookIdWithChapterIdRegex = "/id(\\d+/\\d+)"
    contentPageTemplate = "/id%s.html"
    content {
        document {
            items("#content")
        }
    }
}
}

