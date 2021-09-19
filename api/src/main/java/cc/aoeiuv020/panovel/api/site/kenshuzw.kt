@file:Suppress("UnnecessaryVariable")

package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext

class Kenshuzw : DslJsoupNovelContext() {init {
    // 这网站请求块一点就会503,
    site {
        name = "啃书阁"
        baseUrl = "http://www.kenshuzw.com"
        logo = "http://www.kenshuzw.com/api/images/logo.png"
    }
    search {
        get {
            url = "/modules/article/search.php"
            data {
                "area" to 2
                "searchkey" to it
            }
        }
        document {
            items("div.layout > ul > li") {
                name("> div > h3 > a")
                author("> div > p.author", block = ownText())
            }
        }
    }
    // https://www.kenshuzw.com/xiaoshuo/185033/
    detailPageTemplate = "/xiaoshuo/%s/"
    detail {
        document {
            novel {
                name("div.booktitle > div.name > h1")
                author("div.aboutauthor > p.authorname > a")
            }
            image("div.bigpic > img")
            update(
                "div.book-intro > b:nth-child(7)",
                format = "yyyy-MM-dd",
                block = pickString("\\((.*)\\)")
            )
            introduction("div.book-intro > div")
        }
    }
    // https://www.kenshuzw.com/xiaoshuo/185033/0/
    chaptersPageTemplate = "/xiaoshuo/%s/0/"
    chapters {
        document {
            items("ul.clearfix.chapter-list > li > span > a")
            lastUpdate(
                "div.chapter-hd > p > span:nth-child(2) > em",
                format = "yyyy-MM-dd"
            )
        }
    }
    contentPageTemplate = "/xiaoshuo/%s/"
    content {
        document {
            items("div.article-con", block = ownLines())
        }
    }
}
}

