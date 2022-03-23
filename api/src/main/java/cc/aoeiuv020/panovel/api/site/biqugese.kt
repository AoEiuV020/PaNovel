@file:Suppress("UnnecessaryVariable")

package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.reverseRemoveDuplication

/**
 *
 * Created by AoEiuV020 on 2017.10.08-21:03:33.
 */
class Biqugese : DslJsoupNovelContext() {init {
    site {
        name = "书笔趣阁"
        baseUrl = "http://www.bqxs520.com"
        logo = "https://s3.ax1x.com/2021/01/05/sAadA0.png"
    }
    // http://www.biquge.se/case.php?m=search
    search {
        post {
            url = "/case.php"
            data {
                "m" to "search"
                "key" to it
            }
        }
        document {
            items("#newscontent > div.l > ul > li") {
                name("> span.s2 > a")
                author("> span.s4")
            }
        }
    }
    detailPageTemplate = "/%s/"
    detail {
        document {
            novel {
                name("#info > h1")
                author("#info > p:nth-child(2)", block = pickString("作\\s*者：(\\S*)"))
            }
            image("#fmimg > img")
            update("#info > p:nth-child(4)", format = "yyyy-MM-dd", block = pickString("最后更新：(.*)"))
            introduction("#intro")
        }
    }
    chapters {
        document {
            items("#list > dl > dd > a")
            lastUpdate("#info > p:nth-child(4)", format = "yyyy-MM-dd", block = pickString("最后更新：(.*)"))
        }.reverseRemoveDuplication()
    }
    contentPageTemplate = "/%s.html"
    content {
        document {
            items("#content")
        }
    }
}
}

