package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.ownLinesString
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstIntPattern
import cc.aoeiuv020.panovel.api.firstTwoIntPattern
import cc.aoeiuv020.regex.compileRegex

/**
 * Created by AoEiuV020 on 2018.06.09-19:47:51.
 */
class Zhuaji : DslJsoupNovelContext() {init {
    reason = "这网站连接总超时，"
    upkeep = false
    site {
        name = "爪机书屋"
        baseUrl = "https://www.zhuaji.org"
        logo = "https://www.zhuaji.org/zhuaji/images/header_logo.png"
    }
    search {
        // http://m.zhuaji.org/so.html?keyword=%E9%83%BD%E5%B8%82&t=1
        post {
            url = "//m.zhuaji.org/search.html"
            data {
                "t" to "1"
                "searchkey" to it
            }
        }
        document {
            items(" div.main > ul > li") {
                extra("> a")
                name("> a > p.bookname")
                author("> a > p.author", block = ownText())
            }
        }
    }
    // http://www.zhuaji.org/book/2294
    bookIdRegex = firstIntPattern
    detailPageTemplate = "/book/%s"
    detail {
        document {
            novel {
                name("#book > div.bookright > h3")
                author("#book > div.bookright > p.author", block = pickString("作\\s*者：(\\S*)"))
            }
            image("#book > div.bookleft > div.bookimg > img")
            update("#comments > div.comment_list.new9 > li:nth-child(1) > span", format = "yyyy-MM-dd")
            introduction("#book > div.bookright > p.bookintro") {
                // 段最后有本书详情页地址，
                it.ownLinesString().replace(compileRegex("\\s*www.zhuaji.org/book/\\d+"), "")
            }
        }
    }
    // http://www.zhuaji.org/read/2294/
    chaptersPageTemplate = "/read/%s/"
    chapters {
        document {
            items("#main > div.mulu > dl > dd > a")
        }
    }
    // http://www.zhuaji.org/read/2294/843846.html
    bookIdWithChapterIdRegex = firstTwoIntPattern
    contentPageTemplate = "/read/%s.html"
    content {
        document {
            // 有内嵌链接广告，
            items("#content", block = ownLines())
        }.toMutableList().also {
            // 最后有固定文字，
            it[it.lastIndex] = it.last().replace(compileRegex("百镀一下“[^”]*爪机书屋”最新章节第一时间免费阅读。"), "")
        }

    }

    cookieFilter {
        // 不知道哪里来的cookie，可能服务器在调试什么，加过这个cookie然后就再没有过期，现在有这个cookie直接导致500,
        remove("PHPSESSID")
    }
}
}

