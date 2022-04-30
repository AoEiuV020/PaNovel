package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.ownTextListSplitWhitespace
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstThreeIntPattern

/**
 * Created by AoEiuV020 on 2018.06.02-20:54:12.
 */
class Yllxs : DslJsoupNovelContext() {init {
    reason = "官方搜索不可用，"
    hide = true
    site {
        name = "166小说"
        baseUrl = "http://www.166xs.com"
        logo = "http://m.166xs.com/system/logo.png"
    }
    search {
        get {
            charset = "GBK"
            url = "/modules/article/search.php"
            data {
                "searchkey" to it
                // 加上&page=1可以避开搜索时间间隔的限制，
                // 也可以通过不加载cookies避开搜索时间间隔的限制，
                "page" to "1"
            }
        }
        document {
            if (root.ownerDocument().location().endsWith(".html")) {
                // "http://www.166xs.com/116732.html"
                single {
                    name("#book_left_a > div.book > div.book_info > div.title > h2") {
                        it.ownText()
                    }
                    author("#book_left_a > div.book > div.book_info > div.title > h2 > address", block = pickString("作\\s*者：(\\S*)"))
                }
            } else {
                items("#Updates_list > ul > li") {
                    name("> div.works > a.name")
                    author("> div.author > a", block = pickString("([^ ]*) 作品集"))
                }
            }
        }
    }
    // "http://www.166xs.com/116732.html"
    // "http://www.166xs.com/xiaoshuo/116/116732/"
    // "http://www.166xs.com/xiaoshuo/121/121623/34377467.html"
    // http://www.166xs.com/xiaoshuo/0/121/59420.html
    // 主要就是这个详情页，和其他网站比，这个详情页地址没有取bookId一部分分隔，
    bookIdRegex = "(/xiaoshuo/\\d*)?/(\\d+)"
    bookIdIndex = 1
    detailPageTemplate = "/%s.html"
    detail {
        document {
            novel {
                /*
                <h2>超品相师<address>作者：西域刀客</address></h2>
                 */
                name("#book_left_a > div.book > div.book_info > div.title > h2") {
                    it.ownText()
                }
                author("#book_left_a > div.book > div.book_info > div.title > h2 > address", block = pickString("作\\s*者：(\\S*)"))
            }
            image("#book_left_a > div.book > div.pic > img")
            update("#book_left_a > div.book > div.book_info > div.info > p > span:nth-child(8)", format = "yyyy-MM-dd", block = pickString("更新时间：(.*)"))
            introduction("#book_left_a > div.book > div.book_info > div.intro > p")
        }
    }
    chapterDivision = 1000
    chaptersPageTemplate = "/xiaoshuo/%d/%s"
    chapters {
        // 七个多余的，
        // 三个class="book_btn"，
        // 一个最新章，
        // 三个功能按钮，
        document {
            items("body > dl > dd > a")
        }.drop(3)
    }
    // http://www.166xs.com/xiaoshuo/121/121623/34377471.html
    // http://www.166xs.com/xiaoshuo/31/31008/6750409.html
    bookIdWithChapterIdRegex = firstThreeIntPattern
    contentPageTemplate = "/xiaoshuo/%s.html"
    content {
        document {
            items("p.Book_Text") {
                it.ownTextListSplitWhitespace().dropLastWhile {
                    it == "166小说阅读网"
                }
            }
        }
    }
}
}

