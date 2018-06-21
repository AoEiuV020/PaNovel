package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.ownerPath
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstThreeIntPattern
import cc.aoeiuv020.panovel.api.firstTwoIntPattern

/**
 * Created by AoEiuV020 on 2018.06.06-15:22:06.
 */
class Dajiadu : DslJsoupNovelContext() {init {
    site {
        name = "大家读书院"
        baseUrl = "http://www.dajiadu.net"
        logo = "http://www.dajiadu.net/themes/2100/logo.gif"
    }
    search {
        get {
            // http://www.dajiadu.net/modules/article/searchab.php?searchtype=articlename&searchkey=%B6%BC%CA%D0&page=1
            charset = "GBK"
            url = "/modules/article/searchab.php"
            data {
                "searchtype" to "articlename"
                "searchkey" to it
                // 加上&page=1可以避开搜索时间间隔的限制，
                // 也可以通过不加载cookies避开搜索时间间隔的限制，
                "page" to "1"
            }
        }
        document {
            if (root.ownerPath().startsWith("/files/article/info/")) {
                single {
                    name("#content > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > span:nth-child(1) > h1:nth-child(1)")
                    author("#content > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(2) > td:nth-child(2)", block = pickString("作\\s*者：(\\S*)"))
                }
            } else {
                items(".grid > tbody:nth-child(2) > tr:not(:nth-child(1))") {
                    name("> td:nth-child(1) > a:nth-child(1)")
                    author("> td:nth-child(3)")
                }
            }
        }
    }
    // http://www.dajiadu.net/files/article/info/38/38909.htm
    bookIdRegex = firstTwoIntPattern
    detailPageTemplate = "/files/article/info/%s.htm"
    detail {
        document {
            novel {
                name("#content > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > span:nth-child(1) > h1:nth-child(1)")
                author("#content > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(2) > td:nth-child(2)", block = pickString("作\\s*者：(\\S*)"))
            }
            image("#content > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(2) > td:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(2) > img:nth-child(1)")
            update("#content > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(3) > td:nth-child(1)", format = "yyyy-MM-dd", block = pickString("最后更新：(.*)"))
            introduction("#content > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(2) > td:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(2)", block = ownLinesString())
        }
    }
    // http://www.dajiadu.net/files/article/html/38/38909/index.html
    chaptersPageTemplate = "/files/article/html/%s/index.html"
    chapters {
        document {
            items("#booktext > ul > li > a")
        }
    }
    // http://www.dajiadu.net/files/article/html/38/38909/11492189.html
    bookIdWithChapterIdRegex = firstThreeIntPattern
    contentPageTemplate = "/files/article/html/%s.html"
    content {
        document {
            /*
            <span class="copy">更多精彩小说，欢迎访问大家读书院 http://www.dajiadu.net</span>
             */
            // 最后一段最后可能有一两个无意义字母，搞不定，
            items("#center > div:not(#centerin)", block = ownLinesSplitWhitespace())
        }
    }
}
}

