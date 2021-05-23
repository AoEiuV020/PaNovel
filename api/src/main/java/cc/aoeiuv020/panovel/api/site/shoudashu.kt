@file:Suppress("UnnecessaryVariable")

package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstThreeIntPattern
import cc.aoeiuv020.panovel.api.firstTwoIntPattern
import java.net.URL

class Shoudashu : DslJsoupNovelContext() {init {
    site {
        name = "手打小说网"
        baseUrl = "https://www.shoudashu.com"
        logo = "https://www.shoudashu.com/17mb/images/logo.png"
    }
    // https://www.shoudashu.com/modules/article/search.php  searchkey=%B6%BC%CA%D0&t_btnsearch=
    cookieFilter {
        // 通过不加载cookie避开搜索时间间隔的限制，
        // 也可以用page绕过，
        remove("jieqiVisitTime")
    }
    search {
        get {
            url = "/modules/article/search.php"
            charset = "gbk"
            data {
                "searchkey" to it
                "t_btnsearch" to ""
            }
        }
        document {
            if (URL(
                    root.ownerDocument().location()
                ).path.startsWith("/modules/article/search.php")
            ) {
                items("body > div.main.w > ul > li") {
                    name("> p.d1 > a")
                    author("> p.d2 > span.author > a")
                }
            } else {
                single {
                    name("div.articleinfo h1")
                    author("span.author > a")
                }
            }
        }
    }
    // https://www.shoudashu.com/304/304586/
    bookIdRegex = firstTwoIntPattern
    detailPageTemplate = "/%s/"
    detail {
        document {
            novel {
                name("div.articleinfo h1")
                author("span.author > a")
            }
            image("div.articleinfo  img")
            introduction("div.articleinfo > div.r > div.l2 > p.p3")
        }
    }
    chapters {
        document {
            items("div.chapterlist > ul > li > a")
        }
    }
    // https://www.shoudashu.com/277/277083/66921365.html
    bookIdWithChapterIdRegex = firstThreeIntPattern
    contentPageTemplate = "/%s.html"
    content {
        document {
            items("#content")
        }
    }
}
}

