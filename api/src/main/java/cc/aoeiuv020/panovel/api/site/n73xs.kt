package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.ownLinesString
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstThreeIntPattern

/**
 * Created by AoEiuV020 on 2018.06.04-17:23:51.
 */
class N73xs : DslJsoupNovelContext() {init {
    site {
        name = "73文学"
        baseUrl = "http://www.73wx.com/"
        logo = "https://imgsa.baidu.com/forum/w%3D580/sign=d8f0b6300f3b5bb5bed720f606d2d523/958c4b160924ab1867ef56ac39fae6cd79890b9f.jpg"
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
            if (root.ownerDocument().location().endsWith("/")) {
                single {
                    name(".zhuyan > ul:nth-child(1) > li:nth-child(1)", block = pickString("书名：(\\S*)"))
                    author(".zhuyan > ul:nth-child(1) > li:nth-child(2)", block = pickString("作者：(\\S*)"))
                }
            } else {
                items("#content > table > tbody > tr:not(:nth-child(1)):not(:nth-last-child(1))") {
                    name("> td.odd > a")
                    author("> td.odd", block = pickString(" / (\\S*)"))
                }
            }
        }
    }
    // http://www.73wx.com/book/32252/
    // http://www.73wx.com/32/32252/
    // http://www.73wx.com/32/32252/8967417.html
    bookIdRegex = "/((book)|(\\d+))/(\\d+)"
    bookIdIndex = 3
    // 详情页连简介都没有，直接用章节列表页，
    // 但是搜索结果可能跳到详情页，bookId不得不支持，
    detailDivision = 1000
    detailPageTemplate = "/%d/%s/"
    detail {
        document {
            val div = element("#info")
            val title = element("> div.infotitle", parent = div)
            novel {
                name("> h1", parent = title)
                author("> i:nth-child(2)", parent = title, block = pickString("作者：(\\S*)"))
            }
            image("#fmimg img")
            introduction("> div.intro", parent = div) {
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
            // 这网站详情页更新时间是js另外拿的，
            // http://www.73wx.com/modules/article/pd_uptime.php?id=32252
        }
    }
    // http://www.73wx.com/32/32252/
    chapters {
        document {
            items("#list > dl > dd > a")
        }
    }
    // http://www.73wx.com/32/32252/8967417.html
    bookIdWithChapterIdRegex = firstThreeIntPattern
    contentPageTemplate = "/%s.html"
    content {
        document {
            items("#content > p")
        }
    }

    cookieFilter {
        removeAll {
            // 删除cookie绕开搜索时间间隔限制，
            it.name() == "jieqiVisitTime"
        }
    }
}
}

