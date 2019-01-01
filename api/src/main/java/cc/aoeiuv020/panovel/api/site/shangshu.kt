package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.ownTextListSplitWhitespace
import cc.aoeiuv020.base.jar.splitWhitespace
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstThreeIntPattern
import cc.aoeiuv020.panovel.api.firstTwoIntPattern
import cc.aoeiuv020.regex.compileRegex

/**
 * Created by AoEiuV020 on 2018.06.07-14:02:53.
 */
class Shangshu : DslJsoupNovelContext() {init {
    hide = true
    site {
        name = "上书网"
        baseUrl = "https://www.shangshu.cc"
        logo = "https://www.shangshu.cc/themes/shangshu/logo.png"
    }
    search {
        get {
            // https://www.shangshu.cc/modules/article/search.php?searchtype=articlename&searchkey=%B6%BC%CA%D0&page=1
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
            single("^/\\d+/\\d+") {
                name("#index > div.ListCon > div.BookInfo > table:nth-child(1) > tbody > tr > td:nth-child(2) > h1", block = pickString("(\\S*)最新章节"))
                author("#index > div.ListCon > div.BookInfo > table:nth-child(1) > tbody > tr > td:nth-child(2) > h2 > a")
            }
            items("#content > table > tbody > tr:not(:nth-child(1))") {
                name("> td:nth-child(1) > a")
                author("> td:nth-child(3)")
            }
        }
    }
    // https://www.shangshu.cc/77/77121/
    bookIdRegex = firstTwoIntPattern
    detailPageTemplate = "/%s/"
    detail {
        document {
            novel {
                name("#index > div.ListCon > div.BookInfo > table:nth-child(1) > tbody > tr > td:nth-child(2) > h1", block = pickString("(\\S*)最新章节"))
                author("#index > div.ListCon > div.BookInfo > table:nth-child(1) > tbody > tr > td:nth-child(2) > h2 > a")
            }
            image("#index > div.ListCon > div.BookInfo > table:nth-child(1) > tbody > tr > td:nth-child(1) > img")
            // 时间是js加载的，
            introduction("#index > div.ListCon > div.BookInfo > table:nth-child(1) > tbody > tr > td:nth-child(2) > h3") {
                it.ownText().removePrefix("介绍：").splitWhitespace()
                        .joinToString("\n")
            }
        }
    }
    chapters {
        /*
<ul class="ListRow">
<li><a href="https://www.shangshu.cc/77/77121/48390667.html" title="餮仙传人在都市">餮仙传人在都市www.shangshu.cc首发</a></li>
<li>[<a href="/newmessage.php?tosys=1&amp;title=举报-餮仙传人在都市更新太慢&amp;content=小说《餮仙传人在都市》更新慢了，别的站已经更新了（请告诉我们网址，以便我们跟进）：" target="_blank">更新提醒</a>]</li>
</ul>
         */
        document {
            items("#index > div.ListCon > ul > li > a:not([title]):not([target])")
        }
    }
    // https://www.shangshu.cc/77/77121/46738792.html
    bookIdWithChapterIdRegex = firstThreeIntPattern
    contentPageTemplate = "/%s.html"
    content {
        /*
&nbsp;&nbsp;&nbsp;&nbsp;(<a href="http://www..com" target="_blank">http://www..com</a>)
( 餮仙传人在都市  https://www.shangshu.cc/77/77121/ )<!--章节内容结束--></div>
         */
        document {
            items("#content") {
                it.textNodes()
                        .also { it.lastOrNull()?.let { it.text(it.text().replace(compileRegex("\\( \\S+ http[^)]*\\)$"), "")) } }
                        .flatMap { it.ownTextListSplitWhitespace() }
            }
        }
    }
}
}

