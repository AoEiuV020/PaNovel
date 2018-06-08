package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstIntPattern
import cc.aoeiuv020.panovel.api.firstTwoIntPattern
import java.net.URL

/**
 * Created by AoEiuV020 on 2018.06.05-18:50:39.
 */
class Wukong : DslJsoupNovelContext() {init {
    site {
        name = "悟空看书"
        baseUrl = "http://www.wukong.la"
        logo = "https://imgsa.baidu.com/forum/w%3D580/sign=95ca1b6f75310a55c424defc87474387/930dadd12f2eb938f9acfb6ed9628535e7dd6f50.jpg"
    }
    search {
        get {
            // http://www.biqugebook.com/modules/article/search.php?searchtype=articlename&searchkey=%B6%BC%CA%D0&page=1
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
                    name("body > div.container.body-content > div:nth-child(3) > div > div > div.col-md-10 > h1")
                    author("body > div.container.body-content > div:nth-child(3) > div > div > div.col-md-10 > p.booktag > a:nth-child(1)")
                }
            } else {
                items("body > div.container.body-content > div.panel.panel-default > table > tbody > tr:not(:nth-child(1))") {
                    name("> td:nth-child(1) > a")
                    author("> td:nth-child(3)")
                }
            }
        }
    }
    // http://www.wukong.la/book/885/
    bookIdRegex = firstIntPattern
    detailPageTemplate = "/book/%s/"
    detail {
        document {
            novel {
                name("body > div.container.body-content > div:nth-child(3) > div > div > div.col-md-10 > h1")
                author("body > div.container.body-content > div:nth-child(3) > div > div > div.col-md-10 > p.booktag > a:nth-child(1)")
            }
            image("body > div.container.body-content > div:nth-child(3) > div > div > div.col-md-2.col-xs-4.hidden-xs > img")
            update("body > div.container.body-content > div:nth-child(3) > div > div > div.col-md-10 > p:nth-child(3) > span", format = "yyyy-MM-dd HH:mm", block = pickString("（(.*)）"))
            introduction("#bookIntro", block = ownLinesString())
        }
    }
    chapters {
        document {
            items("#list-chapterAll > dl > dd > a")
            lastUpdate("body > div.container.body-content > div:nth-child(3) > div > div > div.col-md-10 > p:nth-child(3) > span", format = "yyyy-MM-dd HH:mm", block = pickString("（(.*)）"))
        }
    }
    bookIdWithChapterIdRegex = firstTwoIntPattern
    contentPageTemplate = "/book/%s.html"
    content {
        document {
            items("#htmlContent", block = ownLinesSplitWhitespace())
        }.dropWhile {
            it.startsWith("悟空看书")
                    || it.startsWith("www.wukong.la")
        }.dropLastWhile {
            it == "-->>"
        }
    }
}
}

