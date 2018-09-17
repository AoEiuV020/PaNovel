package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.ownLinesString
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstThreeIntPattern
import cc.aoeiuv020.panovel.api.firstTwoIntPattern
import cc.aoeiuv020.panovel.api.reverseRemoveDuplication

/**
 * Created by AoEiuV020 on 2018.06.08-19:09:23.
 */
class Shu8 : DslJsoupNovelContext() {init {
    site {
        name = "书吧小说网"
        baseUrl = "http://shu8.cc"
        logo = "http://shu8.cc/static/css/logo.png"
    }
    search {
        get {
            // http://shu8.cc/modules/article/search.php?searchtype=articlename&searchkey=%B6%BC%CA%D0&page=1
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
            single("^/xs/") {
                name("#info > h1")
                author("#info > div.options > span.item.red")
            }
            items("#content > div.toplist > ul > li") {
                name("> p.s1 > a")
                author("> p.s3")
            }
        }
    }
    // http://shu8.cc/xs/61/61453/
    bookIdRegex = firstTwoIntPattern
    detailPageTemplate = "/xs/%s/"
    detail { _ ->
        document {
            novel {
                name("#info > h1")
                author("#info > div.options > span.item.red")
            }
            image("#main > div:nth-child(1) > div.book_info > div.pic > img")
            introduction("#info > h3") { element ->
                element.textNodes().first { it.text().isNotBlank() }
                        .ownLinesString()
            }
        }
    }
    chapters {
        document {
            items("#main > div.box.mt10 > div.book_list > ul > li > a")
        }.reverseRemoveDuplication()
    }
    // http://shu8.cc/xs/61/61453/16115143.html
    bookIdWithChapterIdRegex = firstThreeIntPattern
    contentPageTemplate = "/xs/%s.html"
    content {
        document {
            items("#shu8id")
        }
    }
}
}

