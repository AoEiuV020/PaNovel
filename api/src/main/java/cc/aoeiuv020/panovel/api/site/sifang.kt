package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.encrypt.base64Decode
import cc.aoeiuv020.jsonpath.get
import cc.aoeiuv020.jsonpath.jsonPath
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstIntPattern

class SiFang : DslJsoupNovelContext() {init {
    hide = true
    site {
        name = "四方中文网"
        baseUrl = "http://www.sifangbook.com"
        logo = "http://www.sifangbook.com/static/index/Img/logo.jpg"
    }

    search {
        get {
            url = "/booklibrary/index"
            data {
                "keywords" to it
            }
        }
        document {
            items("#BooklibraryIndex > div.BooklibraryList > ul > li:not([class])") {
                name("> span > a")
                author("> a > dl > dd.nickname")
            }

        }
    }
    detailPageTemplate = "/booklibrary/show/id/%s/"
    detail {
        document {
            novel {
                name("#BooklibraryShow_Left > div.BookContent > div > div.title > span")
                author("#BooklibraryShow_Left > div.BookContent > div > div.title > em") {
                    it.text().removeSuffix(" 著")
                }
            }
            image("#BooklibraryShow_Left > div.BookContent > div > div.pic > img")
            introduction("#BooklibraryShow_Left > div.BookContent > div > div.jianjie")
            update("#BooklibraryShow_Left > div.BookContent > div > div.title > i", format = "yyyy-MM-dd HH:mm:ss", block = pickString("更新时间：(.*)"))
        }
    }
    chaptersPageTemplate = "/booklibrary/readdir/id/%s/"
    chapters {
        document {
            items("#BooklibraryShow_Left > div.BookDir > div.chapter_list > ul > li > a")
            lastUpdate("#BooklibraryShow_Left > div.BookContent > div > div.title > i", format = "yyyy-MM-dd HH:mm:ss", block = pickString("更新时间：(.*)"))
        }
    }
    //http://www.sifangbook.com/booklibrary/membersinglechapter/chapter_id/712.html
    bookIdWithChapterIdRegex = firstIntPattern
    contentPageTemplate = "http://www.sifangbook.com/booklibrary/membersinglechapter/chapter_id/%s.html"
    content {
        get {
            url = getNovelContentUrl(it)
        }
        response {
            it.jsonPath.get<List<String>>("@.data.show_content[*].content").map {
                String(it.base64Decode()).trim()
            }
        }
    }
}
}