package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.ownLinesString
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstThreeIntPattern
import cc.aoeiuv020.panovel.api.firstTwoIntPattern
import cc.aoeiuv020.regex.compileRegex

/**
 * Created by AoEiuV020 on 2018.06.09-20:24:35.
 */
class Uctxt : DslJsoupNovelContext() {init {
    // 网站没了，
    hide = true
    site {
        name = "UC书盟"
        baseUrl = "http://www.uctxt.com"
        logo = "http://www.uctxt.com/images/logo.gif?v1"
    }
    cookieFilter {
        // 通过不加载cookie避开搜索时间间隔的限制，
        // 这网站不能通过指定搜索结果页码避开搜索时间限制，因为搜索结果只有一页，
        remove("jieqiVisitTime")
    }
    search {
        get {
            // http://www.uctxt.com/modules/article/search.php?searchkey=%B6%BC%CA%D0
            charset = "GBK"
            url = "/modules/article/search.php"
            data {
                "searchkey" to it
            }
        }
        document {
            single("^/book/") {
                name("#main > section > div.book-about.clrfix > div > div.l > h1")
                author("#main > section > div.book-about.clrfix > div > div.l > em", block = pickString("作\\s*者：(\\S*)"))
            }
            items("#main > section > div.list-lastupdate > ul > li") {
                name("> span.name > a")
                /*
                <span class="other">步轻尘<small>14702K</small><small>18-06-09 13:28</small><small>连载</small></span>
                 */
                author("> span.other", block = ownText())
            }
        }
    }
    // http://www.uctxt.com/book/19/19280/
    bookIdRegex = firstTwoIntPattern
    detailPageTemplate = "/book/%s/"
    detail {
        document {
            novel {
                name("#main > section > div.book-about.clrfix > div > div.l > h1")
                author("#main > section > div.book-about.clrfix > div > div.l > em", block = pickString("作\\s*者：(\\S*)"))
            }
            update("#main > section > div.book-about.clrfix > p.stats > span.r > i:nth-child(3)", format = "yyyy-MM-dd HH:mm")
            introduction("#main > section > div.book-about.clrfix > p.intro") {
                // 神™广告里的书名不是当前小说的名字，
                it.ownLinesString().replace(compileRegex("\\s*@各位书友要是觉得《[^》]*》还不错的话，请不要忘记向您QQ群和微博里的朋友推荐哦！"), "")
            }
        }
    }
    chapters {
        document {
            items("#main > section > dl > dd > a")
            lastUpdate("#main > section > div.book-about.clrfix > p.stats > span.r > i:nth-child(3)", format = "yyyy-MM-dd HH:mm")
        }
    }
    // http://www.uctxt.com/book/1/1132/349071.html
    bookIdWithChapterIdRegex = firstThreeIntPattern
    contentPageTemplate = "/book/%s.html"
    content {
        document {
            items("#content")
        }.dropLastWhile {
            // 好像没几本有这个广告，
            it == "百度搜索【uc书盟】小说网站，让你体验更新最新最快的章节小说，所有小说秒更新。"
        }
    }
}
}

