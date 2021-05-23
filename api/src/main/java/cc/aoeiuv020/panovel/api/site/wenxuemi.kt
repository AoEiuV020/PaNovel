package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstThreeIntPattern
import cc.aoeiuv020.panovel.api.firstTwoIntPattern

/**
 * Created by AoEiuV020 on 2018.06.03-19:56:19.
 */
class Wenxuemi : DslJsoupNovelContext() {init {
    // 搜索403
    enabled = false
    site {
        name = "文学迷"
        baseUrl = "https://www.wenxuemi.cc"
        logo = "https://www.wenxuemi.cc/images/logo.gif"
    }
    search {
        get {
            url = "/search.php"
            data {
                "q" to it
            }
        }
        document {
            items("div.result-list > div") {
                name("> div.result-game-item-detail > h3 > a")
                author("> div.result-game-item-detail > div > p:nth-child(1) > span:nth-child(2)")
            }
        }
    }
    // https://www.wenxuemi.com/files/article/html/22/22768/
    bookIdRegex = firstTwoIntPattern
    detailPageTemplate = "/files/article/html/%s/"
    detail {
        document {
            novel {
                name("#info > h1")
                author("#info > p:nth-child(2)", block = pickString("作\\s*者：(\\S*)"))
            }
            image("#fmimg > img")
            update("#info > p:nth-child(4)", format = "yyyy-MM-dd HH:mm:ss", block = pickString("最后更新：(.*)"))
            introduction("#intro")
        }
    }
    chapters {
        document {
            items("#list > dl > dd > a")
            lastUpdate("#info > p:nth-child(4)", format = "yyyy-MM-dd HH:mm:ss", block = pickString("最后更新：(.*)"))
        }
    }
    // https://www.wenxuemi.com/files/article/html/22/22768/13420863.html
    bookIdWithChapterIdRegex = firstThreeIntPattern
    contentPageTemplate = "/files/article/html/%s.html"
    content {
        document {
            items("#content")
        }
    }
}
}

