package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.ownLinesString
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstThreeIntPattern
import cc.aoeiuv020.panovel.api.firstTwoIntPattern

/**
 *
 * Created by AoEiuV020 on 2018.03.07-02:42:57.
 */
class Snwx : DslJsoupNovelContext() {init {
    // 这网站搜索功能重定向到百度去了，暂不支持，
    enabled = false
    site {
        name = "少年文学"
        baseUrl = "https://www.snwx3.com"
        logo = "https://www.snwx3.com/xiaoyi/images/logo.gif"
    }
    cookieFilter {
        // 删除cookie绕开搜索时间间隔限制，
        // 这网站只删除jieqiVisitTime已经没用了，
        removeAll {
            httpUrl.encodedPath().startsWith("/modules/article/search.php")
        }
    }
    search {
        get {
            charset = "GBK"
            url = "/modules/article/search.php"
            data {
                "searchkey" to it
            }
        }
        document {
            items("#newscontent > div.l > ul > li") {
                name("> span.s2 > a")
                author("> span.s4")
            }
        }
    }
    bookIdRegex = firstTwoIntPattern
    // https://www.snwxx.com/book/66/66076/
    detailPageTemplate = "/book/%s/"
    detail {
        document {
            val div = element("#info")
            val title = element("> div.infotitle", parent = div)
            novel {
                name("> h1", parent = title)
                author("> i:nth-child(2)", parent = title, block = pickString("作\\s*者：(\\S*)"))
            }
            image("#fmimg > img")
            introduction("> div.intro", parent = div) {
                // 可能没有简介，
                it.textNodes().first {
                    // TextNode不可避免的有空的，
                    !it.isBlank
                            && !it.wholeText.let {
                        // 后面几个TextNode广告包含这些文字，
                        it.startsWith("各位书友要是觉得《${novel?.name}》还不错的话请不要忘记向您QQ群和微博里的朋友推荐哦！")
                                || it.startsWith("${novel?.name}最新章节,${novel?.name}无弹窗,${novel?.name}全文阅读.")
                    }
                }.ownLinesString()
            }
            // 这网站详情页没有更新时间，
        }
    }
    chapters {
        document {
            items("#list > dl > dd > a")
        }
    }
    bookIdWithChapterIdRegex = firstThreeIntPattern
    contentPageTemplate = "/book/%s.html"
    content {
        document {
            items("#BookText")
        }
    }
}
}

