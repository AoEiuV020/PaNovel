@file:Suppress("UnnecessaryVariable")

package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext

/**
 *
 * Created by AoEiuV020 on 2017.10.08-21:03:33.
 */
class Biquge : DslJsoupNovelContext() {init {
    // 连不上了，
    hide = true
    site {
        name = "笔趣阁"
        baseUrl = "https://www.biqubao.com"
        logo =
            "https://imgsa.baidu.com/forum/w%3D580/sign=1d712d8332dbb6fd255be52e3925aba6/d7d2c843fbf2b211dfb81c36c18065380dd78e1b.jpg"
    }
    // https://www.biqubao.com/search.php?q=%E9%83%BD%E5%B8%82
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
    detailPageTemplate = "/book/%s/"
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
    contentPageTemplate = "/book/%s.html"
    content {
        document {
            items("#content")
        }
    }
}
}

