package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.title
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
        baseUrl = "https://www.wukong.la"
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
                    name("h1.bookTitle") {
                        it.ownText()
                    }
                    author("h1.bookTitle > small > a")
                }
            } else {
                items("div.panel-body > div > div:nth-child(1) > div") {
                    name("> div > div.caption > h4 > a") {
                        it.title()
                    }
                    author("> div > div.caption > small", block = pickString("(\\S*) / 著"))
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
                name("h1.bookTitle") {
                    it.ownText()
                }
                author("h1.bookTitle > small > a")
            }
            image("img.img-thumbnail")
            update("div.col-md-10 > p:nth-child(3) > span", format = "yyyy-MM-dd HH:mm", block = pickString("（(.*)）"))
            introduction("#bookIntro", block = ownLinesString())
        }
    }
    chapters {
        document {
            items("#list-chapterAll > dl > dd > a")
            lastUpdate("div.col-md-10 > p:nth-child(3) > span", format = "yyyy-MM-dd HH:mm", block = pickString("（(.*)）"))
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
                    || it == "最新章节！"
        }.dropLastWhile {
            it == "-->>"
        }
    }
}
}

