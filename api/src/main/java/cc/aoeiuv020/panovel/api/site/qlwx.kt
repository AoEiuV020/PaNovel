package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import java.net.URL

/**
 * Created by AoEiuV020 on 2018.05.10-18:11:57.
 */
class Qlwx : DslJsoupNovelContext() { init {
    site {
        name = "齐鲁文学"
        baseUrl = "http://www.76wx.com"
        logo = "http://www.76wx.com/images/book_logo.png"
    }
    search {
        get {
            charset = "GBK"
            url = "/modules/article/search.php"
            data {
                "searchtype" to "articlename"
                "searchkey" to it
                // 加上&page=1可以避开搜索时间间隔的限制，
                // 也可以通过不加载cookies避开搜索时间间隔的限制，
                "page" to "1"
            }
        }
        document {
            if (URL(root.ownerDocument().location()).path.startsWith("/book/")) {
                single {
                    val eInfo = element(query = "#maininfo #info")
                    name("> h1", eInfo)
                    author("> p:nth-child(2)", eInfo, block = pickString("作    者：(\\S*)"))
                }
            } else {
                items("#main > table > tbody > tr:not(:nth-child(1))") {
                    name("td:nth-child(1) > a")
                    author("td:nth-child(3)")
                }
            }
        }
    }
    detailPageTemplate = "/book/%s/"
    detail {
        document {
            val eInfo = element(query = "#maininfo #info")
            novel {
                name("> h1", eInfo)
                author("> p:nth-child(2)", eInfo, block = pickString("作    者：(\\S*)"))
            }
            image("#fmimg > img")
            introduction("#intro > p:not(:nth-last-child(1))")
            update("> p:nth-child(4)", parent = eInfo, format = "yyyy-MM-dd HH:mm:ss", block = pickString("更新时间：(.*)"))
        }
    }
    chapters {
        // 开头 9 章可能是网站上显示的最新章节，和列表最后重复，
        // 但也可能没有这重复的 9 章，
        val list = document {
            items("#list > dl > dd > a")
            lastUpdate("#maininfo #info > p:nth-child(4)", format = "yyyy-MM-dd HH:mm:ss", block = pickString("更新时间：(.*)"))
        }
        var index = 0
        // 以防万一，
        if (list.size == 1) return@chapters list
        // 倒序列表判断是否重复章节，
        // 最后一章被填充了更新时间，第一章重复的没有，所以不能直接==判断NovelChapter对象，
        val reversedList = list.asReversed()
        list.dropWhile {
            (it.extra == reversedList[index].extra).also { ++index }
        }
    }
    // http://www.76wx.com/book/161/892418.html
    contentPageTemplate = "/book/%s.html"
    content {
        document {
            items("#content")
        }
    }
}
}

