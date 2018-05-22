package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstThreeIntPattern
import cc.aoeiuv020.panovel.api.firstTwoIntPattern

/**
 *
 * Created by AoEiuV020 on 2018.03.07-02:42:57.
 */
class Snwx : DslJsoupNovelContext() {init {
    site {
        name = "少年文学"
        baseUrl = "https://www.snwx8.com"
        logo = "https://www.snwx8.com/xiaoyi/images/logo.gif"
    }
    search {
        get {
            url = "/modules/article/search.php?searchkey=${gbk(it)}"
        }
        // 删除cookie绕开搜索时间间隔限制，
        requireNotNull(connection).request().removeCookie("jieqiVisitTime")
        document {
            items("#newscontent > div.l > ul > li") {
                name("> span.s2 > a")
                author("> span.s4")
            }
        }
    }
    bookIdRegex = firstTwoIntPattern
    // https://www.snwx8.com/book/66/66076/
    detailTemplate = "/book/%s/"
    detail {
        document {
            val div = element("#info")
            val title = element("> div.infotitle", parent = div)
            novel {
                name("> h1", parent = title)
                author("> i:nth-child(2)", parent = title, block = pickString("作者：(\\S*)"))
            }
            image("#fmimg > img")
            introduction("> div.intro", parent = div) {
                it.textNodes().first {
                    // TextNode不可避免的有空的，
                    !it.isBlank
                            && !it.wholeText.let {
                        // 后面几个TextNode广告包含这些文字，
                        it.startsWith("各位书友要是觉得《${novel?.name}》还不错的话请不要忘记向您QQ群和微博里的朋友推荐哦！")
                                || it.startsWith("${novel?.name}最新章节,${novel?.name}无弹窗,${novel?.name}全文阅读.")
                    }
                }.ownTextList().joinToString()
            }
            // 这网站详情页没有更新时间，
        }
    }
    chapters {
        document {
            items("#list > dl > dd > a")
        }
    }
    chapterIdRegex = firstThreeIntPattern
    contentTemplate = "/book/%s.html"
    content {
        document {
            items("#BookText")
        }
    }
}
}

