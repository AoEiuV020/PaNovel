package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstThreeIntPattern
import cc.aoeiuv020.panovel.api.firstTwoIntPattern

/**
 * Created by AoEiuV020 on 2018.06.06-16:47:20.
 */
class Liewen : DslJsoupNovelContext() {init {
    site {
        name = "猎文网"
        baseUrl = "https://www.liewen.cc"
        logo = "https://www.liewen.cc/images/logo.gif"
    }
    search {
        get {
            // https://www.liewen.cc/search.php?keyword=%E9%83%BD%E5%B8%82
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
    // https://www.liewen.cc/b/5/5024/
    bookIdRegex = firstTwoIntPattern
    detailPageTemplate = "/b/%s/"
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
    // https://www.liewen.cc/b/5/5024/13777631.html
    bookIdWithChapterIdRegex = firstThreeIntPattern
    contentPageTemplate = "/b/%s.html"
    content {
        document {
            /*
            有混杂这样的广告，不好处理，无视，
            ?  ?猎文  ｗ?ｗ?ｗ?． ｌ?ｉ?ｅ?ｗ?ｅ?ｎ?．ｃｃ
             */
            items("#content")
        }
    }
}
}

