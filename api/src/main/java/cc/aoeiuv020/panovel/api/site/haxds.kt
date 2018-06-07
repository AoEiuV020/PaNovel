package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstThreeIntPattern

/**
 * Created by AoEiuV020 on 2018.06.07-14:55:27.
 */
class Haxds : DslJsoupNovelContext() {init {
    // 这网站好像有点污，
    enabled = false
    site {
        name = "海岸线文学"
        baseUrl = "https://www.haxds.com"
        logo = "https://www.haxds.com/static/css/logo.png"
    }
    search {
        get {
            // http://zhannei.baidu.com/cse/search?s=12437495264296208885&q=%E9%83%BD%E5%B8%82
            // 这网站电脑搜索用的是百度的，不行，
            // https://m.haxds.com/modules/article/waps.php?type=articlename&s=%E9%83%BD%E5%B8%82&submit=
            url = "//m.haxds.com/modules/article/waps.php"
            data {
                "type" to "articlename"
                "s" to it
            }
        }
        document {
            items("body > div:nth-child(5) > p") {
                name("> a.blue")
                author("> a:nth-child(3)")
            }
        }
    }
    // https://www.haxds.com/files/article/info/72/72169.htm
    // https://www.haxds.com/files/article/html/72/72169/index.html
    // https://www.haxds.com/files/article/html/72/72169/11460024.html
    // https://m.haxds.com/info/70596.htm
    // https://m.haxds.com/mulu/70596.html
    // https://m.haxds.com/yuedu/70596/11035616.html
    bookIdRegex = "/(files/article/(info|html)/\\d+|(info|mulu|yuedu))/(\\d+)"
    bookIdIndex = 3
    detailDivision = 1000
    detailPageTemplate = "/files/article/info/%d/%s.htm"
    detail {
        document {
            novel {
                name("#content > div:nth-child(1) > div > div.book-info > div.book-title > h1")
                author("#content > div:nth-child(1) > div > div.book-info > div.book-title > em", block = pickString("作者：(\\S*)"))
            }
            image("#content > div:nth-child(1) > div > div.book-img > img")
            update("#content > div:nth-child(1) > div > div.book-info > p.book-stats", format = "yyyy-MM-dd", block = pickString("更新时间：(.*)"))
            introduction("#content > div:nth-child(1) > div > div.book-info > p.book-intro")
        }
    }
    // https://www.haxds.com/files/article/html/72/72169/index.html
    chapterDivision = 1000
    chaptersPageTemplate = "/files/article/html/%d/%s/index.html"
    chapters {
        document {
            items("#main > div > dl > dd > a")
        }
    }
    // https://www.haxds.com/files/article/html/72/72169/49571282.html
    bookIdWithChapterIdRegex = firstThreeIntPattern
    contentPageTemplate = "/files/article/html/%s.html"
    content {
        document {
            items("#BookText") {
                // 第一段是章节名，
                it.textNodes().drop(1)
                        .mapNotNull { it.textNotBlank() }
            }
        }
    }
}
}

