package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.textListSplitWhitespace
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstIntPattern
import cc.aoeiuv020.panovel.api.firstThreeIntPattern

/**
 * Created by AoEiuV020 on 2018.06.07-13:30:33.
 */
class Zaidudu : DslJsoupNovelContext() {init {
    /*
    网站变成其他行业了，应该是倒闭了域名卖了，
     */
    hide = true
    site {
        name = "再读读"
        baseUrl = "http://www.zaidudu.net"
        logo = "http://www.zaidudu.net/images/logo.png"
    }
    search {
        get {
            // http://www.zaidudu.net/modules/article/search.php?searchtype=articlename&searchkey=%B6%BC%CA%D0&page=1
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
            single("^/book/") {
                name("#post-title > h1")
                author("#post-mate > li:nth-child(1)", block = pickString("作\\s*者：(\\S*)"))
            }
            items("#wrap > table > tbody > tr:not(:nth-child(1))") {
                name("> td:nth-child(1) > a")
                author("> td:nth-child(3)")
            }
        }
    }
    // http://www.zaidudu.net/book/263816.html
    bookIdRegex = firstIntPattern
    detailPageTemplate = "/book/%s.html"
    detail {
        document {
            novel {
                name("#post-title > h1")
                author("#post-mate > li:nth-child(1)", block = pickString("作\\s*者：(\\S*)"))
            }
            image("#pic-fm > dl > dt > img")
            update("#post-mate > li:nth-child(3)", format = "更新时间：yyyy-MM-dd")
            introduction("#articledesc") {
                // 不是每本书都有，大概是互相抄出现的，
                it.textListSplitWhitespace().dropLastWhile {
                    it.startsWith("各位书友要是觉得《${novel?.name}》还不错的话请不要忘记向您QQ群和微博里的朋友推荐哦！")
                }
                        .joinToString("\n")
            }
        }
    }
    // http://www.zaidudu.net/mulu/0/133/
    chapterDivision = 1000
    chaptersPageTemplate = "/mulu/%d/%s/"
    chapters {
        document {
            items("#at > tbody > tr > td > a")
        }
    }
    // http://www.zaidudu.net/mulu/0/133/61990904.html
    bookIdWithChapterIdRegex = firstThreeIntPattern
    contentPageTemplate = "/mulu/%s.html"
    content {
        document {
            items("#contents")
        }
    }
}
}

