package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.ownTextListSplitWhitespace
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstThreeIntPattern
import cc.aoeiuv020.panovel.api.firstTwoIntPattern
import java.net.URL

/**
 * Created by AoEiuV020 on 2018.06.03-21:27:05.
 */
class N7dsw : DslJsoupNovelContext() {init {
    // 不咋地，随机握手失败，搜索可能502,
    enabled = false
    site {
        name = "7度书屋"
        baseUrl = "https://www.7dsw.com"
        logo = "https://www.7dsw.com/web8/images/logo.gif"
    }
    search {
        get {
            // https://www.7dsw.com/modules/article/search.php?searchkey=%B6%BC%CA%D0&page=1
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
            if (URL(root.ownerDocument().location()).path.startsWith("/book/")) {
                single {
                    name("#info > div.infotitle > h1")
                    author("#info > div.infotitle > i:nth-child(2)", block = pickString("作\\s*者：(\\S*)"))
                }
            } else {
                items("#content > table > tbody > tr:not(:nth-child(1))") {
                    name("> td:nth-child(1) > a")
                    author("> td:nth-child(3)")
                }
            }
        }
    }
    // https://www.7dsw.com/book/0/654/
    bookIdRegex = firstTwoIntPattern
    detailPageTemplate = "/book/%s/"
    detail {
        document {
            novel {
                name("#info > div.infotitle > h1")
                author("#info > div.infotitle > i:nth-child(2)", block = pickString("作\\s*者：(\\S*)"))
            }
            image("#fmimg > img")
            introduction("#info > div.intro") {
                it.textNodes().first {
                    // TextNode不可避免的有空的，
                    !it.isBlank
                            && !it.text().let {
                        // 后面几个TextNode广告包含这些文字，
                        it.startsWith("各位书友要是觉得《${novel?.name}》还不错的话请不要忘记向您QQ群和微博里的朋友推荐哦！")
                                || it.startsWith("${novel?.name}最新章节,${novel?.name}无弹窗,${novel?.name}全文阅读.")
                    }
                }.ownTextListSplitWhitespace().dropLastWhile {
                    it.startsWith("各位书友要是觉得《${novel?.name}》还不错的话请不要忘记向您QQ群和微博里的朋友推荐哦！")
                }.joinToString("\n")
            }
        }
    }
    chapters {
        document {
            items("#list > dl > dd > a")
        }
    }
    // https://www.7dsw.com/book/0/654/178709.html
    bookIdWithChapterIdRegex = firstThreeIntPattern
    contentPageTemplate = "/book/%s.html"
    content {
        document {
            items("#BookText")
        }
    }
}
}

