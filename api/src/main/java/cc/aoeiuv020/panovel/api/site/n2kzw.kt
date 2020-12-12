package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.atry.tryOrNul
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstThreeIntPattern
import cc.aoeiuv020.panovel.api.firstTwoIntPattern
import cc.aoeiuv020.regex.compilePattern
import org.jsoup.nodes.Element

/**
 * Created by AoEiuV020 on 2018.06.08-18:47:21.
 */
class N2kzw : DslJsoupNovelContext() {init {
    site {
        name = "2k中文网"
        baseUrl = "https://www.2kzw.com"
        logo = "https://www.2kzw.com/17mb/images/logo.png"
    }
    // 这网站所有名字都是/遮天(精校版)/官道无疆(校对版)/
    val pickName = { e: Element ->
        e.text().replace(compilePattern("\\(\\S+版\\)$").toRegex(), "")
    }
    search {
        get {
            // https://www.2kzw.com/search/
            url = "/search/"
            data {
                "searchkey" to it
            }
        }
        document {
            items("div.category-div") {
                name("> div > div.flex.flex-between.commend-title > a", block = pickName)
                author("> div > div.flex.flex-between.commend-title > span")
            }
        }
    }
    // http://www.2kzw.com/5/5798/
    bookIdRegex = firstTwoIntPattern
    detailPageTemplate = "/%s/"
    detail {
        document {
            novel {
                name("div.w100 > h1", block = pickName)
                author("div.w100 > div.w100 > span", block = pickString("作\\s*者：(\\S*)"))
            }
            update("div[class=dispc] > span", format = "yyyy-MM-dd HH:mm:ss", block = pickString("最后更新：(.*)"))
            // https://www.2kzw.com/images/14/14196.jpg
            tryOrNul {
                image = site.baseUrl + "/images/$it.jpg"
            }
            introduction("div.info-main-intro")
        }
    }
    chapters {
        document {
            items("div.container.border3-2.mt8.mb20 > div > a")
            lastUpdate("div[class=dispc] > span", format = "yyyy-MM-dd HH:mm:ss", block = pickString("最后更新：(.*)"))
        }
    }
    // https://www.2kzw.com/38/38424/30595033.html
    bookIdWithChapterIdRegex = firstThreeIntPattern
    contentPageTemplate = "/%s.html"
    content {
        // 有些小说第一行是章节名，但不是所有，所以不能删除第一行，
        document {
            items("#article")
        }
    }

    cookieFilter {
        removeAll {
            // 删除cookie绕开搜索时间间隔限制，
            it.name() == "ss_search_delay"
        }
    }
}
}

