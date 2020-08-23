package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.ownTextListSplitWhitespace
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstThreeIntPattern
import cc.aoeiuv020.panovel.api.firstTwoIntPattern
import cc.aoeiuv020.panovel.api.reverseRemoveDuplication

/**
 *
 * Created by AoEiuV020 on 2018.03.14-01:35:36.
 */
class Syxs : DslJsoupNovelContext() {init {
    site {
        name = "31小说"
        baseUrl = "http://www.31xiaoshuo.org"
        logo = "https://imgsa.baidu.com/forum/w%3D580/sign=e8432cdf06d162d985ee621421dfa950/47e082d8bc3eb135d9b1d5a0aa1ea8d3fc1f44a6.jpg"
    }
    search {
        get {
            url = "/search.php"
            data {
                "keyword" to it
            }
        }
        document {
            /*
            <tr>
                <td class="odd">[<a href="/list/3/1.html">都市</a>] <a href="http://www.31xs.com/13/13704/" target="_blank"><font color="red">都市</font>之极品仙官</a></td>
                <td class="odd"><a href="http://www.31xs.com/13/13704/10358379.html" target="_blank">第1037章 开杀戒</a></td>
                <td class="even">八方风云</td>
                <td class="odd">2018-05-21 05:26:24</td>
            </tr>
             */
            items("#bookcase_list > tr") {
                name("> td > a")
                author("> td:nth-child(4)")
            }
        }
    }
    bookIdRegex = firstTwoIntPattern
    // http://www.31xs.net/13/13011/
    detailPageTemplate = "/%s/"
    detail { _ ->
        document {
            val div = root.requireElement("#info")
            novel {
                name("> h1", parent = div)
                author("> p:nth-child(2)", parent = div, block = pickString("作\\s*者：(\\S*)"))
            }
            image("#fmimg > img")
            introduction("#intro") {
                it.ownTextListSplitWhitespace().joinToString("\n")
            }
            update("head > meta[property=og:novel:update_time]", format = "yyyy-MM-dd HH:mm:ss") {
                it.attr("content")
            }
        }
    }
    chapters { _ ->
        document {
            items("#list > dl > dd > a")
        }.reverseRemoveDuplication().dropWhile { it.extra.isBlank() }
    }
    bookIdWithChapterIdRegex = firstThreeIntPattern
    // http://www.31xs.net/13/13011/8981866.html
    contentPageTemplate = "/%s.html"
    content {
        document {
            items("#content > p")
        }
    }
}
}

