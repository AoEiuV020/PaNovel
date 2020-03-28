@file:Suppress("UnnecessaryVariable")

package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.absHref
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstThreeIntPattern
import cc.aoeiuv020.panovel.api.firstTwoIntPattern
import java.net.URL

class N168kanshu : DslJsoupNovelContext() {init {
    site {
        name = "168看书网"
        baseUrl = "https://www.168kanshu.com"
        logo = "https://www.168kanshu.com/static/css/logo.gif?qsv=192"
    }
    search {
        get {
            charset = "GBK"
            // https://www.168kanshu.com/modules/article/search.php?searchkey=%B6%BC%CA%D0
            url = "/modules/article/search.php"
            data {
                "searchkey" to it
            }
        }
        document {
            if (URL(root.ownerDocument().location()).path.startsWith("/xs")) {
                single {
                    name("#info > h1")
                    author("#info > div.options > span.item.red")
                }
            } else {
                items("#content > div.toplist > ul > li") {
                    name("> p.s1 > a")
                    author("> p.s3")
                }
            }
        }
    }
    // https://www.168kanshu.com/xs/98/98881/
    detailPageTemplate = "/xs/%s/"
    bookIdRegex = firstTwoIntPattern
    detail {
        document {
            novel {
                name("#info > h1")
                author("#info > div.options > span.item.red")
            }
            image("div.book_info > div.pic > img")
            introduction("#info > h3")
        }
    }
    chapters {
        document {
            items("div.book_list > ul > li > a")
        }
    }
    // https://www.168kanshu.com/xs/98/98881/46980584.html
    contentPageTemplate = "/xs/%s.html"
    bookIdWithChapterIdRegex = firstThreeIntPattern
    content {
        var next: String? = getNovelContentUrl(it)
        val ret = mutableListOf<String>()
        while (next != null) {
            get {
                url = next
            }
            document {
                items("#htmlContent")
                // 加载下一页，
                next = root.selectFirst("#jsnc_l > div > div.chapter_Turnpage > a.next.pager_next:contains(下一页)")?.absHref()
            }.let { ret.addAll(it) }
        }
        ret
    }
}
}

