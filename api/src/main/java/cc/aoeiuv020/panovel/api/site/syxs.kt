package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstThreeIntPattern
import cc.aoeiuv020.panovel.api.firstTwoIntPattern

/**
 *
 * Created by AoEiuV020 on 2018.03.14-01:35:36.
 */
class Syxs : DslJsoupNovelContext() {init {
    site {
        name = "31小说"
        baseUrl = "http://www.31xs.net"
        logo = "https://imgsa.baidu.com/forum/w%3D580/sign=e8432cdf06d162d985ee621421dfa950/47e082d8bc3eb135d9b1d5a0aa1ea8d3fc1f44a6.jpg"
    }
    search {
        get {
            charset = "GBK"
            url = "/search.php"
            data {
                "keywords" to it
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
            items("#content > table > tbody > tr:not(:nth-child(1))") {
                name(" > td:nth-child(1) > a:nth-child(2)")
                author(" > td.even")
            }
        }
    }
    bookIdRegex = firstTwoIntPattern
    // http://www.31xs.net/13/13011/
    detailPageTemplate = "/%s/"
    detail {
        document {
            val div = root.requireElement("#info")
            novel {
                name("> h1", parent = div)
                author("> p:nth-child(2)", parent = div, block = pickString("作    者：(\\S*)"))
            }
            image("#fmimg > img")
            introduction("#intro") {
                it.ownTextList().joinToString("\n")
            }
            update("head > meta[property=og:novel:update_time]", format = "yyyy-MM-dd HH:mm:ss") {
                it.attr("content")
            }
        }
    }
    chapters {
        // 开头 9 章可能是网站上显示的最新章节，和列表最后重复，
        // 但也可能没有这重复的 9 章，
        // 接着两章是js脚本，拿到的地址会是空，
        val list = document {
            items("#list > dl > dd > a")
        }
        var index = 0
        // 以防万一，
        if (list.size == 1) return@chapters list
        // 倒序列表判断是否重复章节，
        val reversedList = list.asReversed()
        list.dropWhile {
            (it == reversedList[index]
                    || it.extra.isBlank()).also { ++index }
        }
    }
    chapterIdRegex = firstThreeIntPattern
    // http://www.31xs.net/13/13011/8981866.html
    contentPageTemplate = "/%s.html"
    content {
        document {
            items("#content > p")
        }
    }
}
}

