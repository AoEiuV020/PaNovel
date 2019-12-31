package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.textList
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstThreeIntPattern
import cc.aoeiuv020.panovel.api.firstTwoIntPattern

/**
 * Created by AoEiuV020 on 2018.06.03-18:12:36.
 */
class Guanshuwang : DslJsoupNovelContext() {init {
    site {
        name = "官术网"
        baseUrl = "https://www.biyuwu.cc"
        logo = "http://www.biyuwu.cc/images/logo.gif"
    }
    search {
        get {
            // https://www.biyuwu.cc/search.php?q=%E4%BF%AE%E7%9C%9F
            url = "/search.php"
            data {
                "q" to it
            }
        }
        document {
            items("body > div.result-list > div") {
                name("> div.result-game-item-detail > h3 > a")
                author("> div.result-game-item-detail > div > p:nth-child(1) > span:nth-child(2)")
            }
        }
    }
    // http://www.3dllc.cc/html/86/86047/
    bookIdRegex = firstTwoIntPattern
    detailPageTemplate = "/html/%s/"
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
            /*
            <a href="/book/1196/443990.html">第一章 觉醒日</a>
             */
            items("#list > dl > dd > a")
            lastUpdate("#info > p:nth-child(4)", format = "yyyy-MM-dd HH:mm:ss", block = pickString("最后更新：(.*)"))
        }
    }
    // http://www.3dllc.cc/html/86/86047/2336.html
    bookIdWithChapterIdRegex = firstThreeIntPattern
    contentPageTemplate = "/html/%s.html"
    content {
        document {
            items("#content", block = { e ->
                // 这网站正文第一行开关都有个utf8bom,不会被当成空白符过滤掉，
                e.textList().map { it.removePrefix("\ufeff").trimStart() }
            })
        }
    }
}
}

