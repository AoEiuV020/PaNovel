package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstThreeIntPattern

/**
 * Created by AoEiuV020 on 2018.06.06-19:24:03.
 */
class Lewen123 : DslJsoupNovelContext() {init {
    // 这网站章节有大量重复，
    enabled = false
    site {
        name = "乐文小说"
        baseUrl = "http://www.lewen123.com"
        logo = "http://www.lewen123.com/themes/lwxs/logo.png"
    }
    // 搜索结果如果只有一本，会跳到这个域名，估计是网站旧域名，
    hostList += "www.lwxiaoshuo.com"
    search {
        get {
            // http://www.lewen123.com/modules/article/search.php?searchtype=articlename&searchkey=%B6%BC%CA%D0&page=1
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
            single("^/lewen/") {
                name("#detail-box > div > div.ui_bg6 > div.box_intro > div.box_info > table > tbody > tr:nth-child(1) > td > div.bookname > h1", block = ownLinesString())
                author("#detail-box > div > div.ui_bg6 > div.box_intro > div.box_info > table > tbody > tr:nth-child(1) > td > div.bookname > h1 > em", block = pickString("作\\s*者：(\\S+)"))
            }
            items("#alistbox") {
                name("> div.info > div.title > h2 > a")
                author("> div.info > div.title > span", block = pickString("作\\s*者：(\\S+)"))
            }
        }
    }
    // http://www.lwxiaoshuo.com/lewen/57260.html
    bookIdRegex = "/(lewen|\\d+)/(\\d+)"
    bookIdIndex = 1
    detailPageTemplate = "/lewen/%s.html"
    detail {
        document {
            novel {
                name("#detail-box > div > div.ui_bg6 > div.box_intro > div.box_info > table > tbody > tr:nth-child(1) > td > div.bookname > h1", block = ownLinesString())
                author("#detail-box > div > div.ui_bg6 > div.box_intro > div.box_info > table > tbody > tr:nth-child(1) > td > div.bookname > h1 > em", block = pickString("作\\s*者：(\\S+)"))
            }
            image("#detail-box > div > div.ui_bg6 > div.box_intro > div.pic > img")
            update("#detail-box > div > div.ui_bg6 > div.box_intro > div.box_info > table > tbody > tr:nth-child(6) > td:nth-child(4)", format = "更新时间：yyyy-MM-dd")
            introduction("#detail-box > div > div.ui_bg6 > div.box_intro > div.box_info > table > tbody > tr:nth-child(3) > td > div", block = ownLinesString())
        }
    }
    // http://www.lewen123.com/72/72302/index.html
    chapterDivision = 1000
    chaptersPageTemplate = "/%d/%s/index.html"
    chapters {
        document {
            items("#defaulthtml4 > table > tbody > tr > td > div > a")
        }
    }
    // http://www.lewen123.com/72/72302/19982095.html
    bookIdWithChapterIdRegex = firstThreeIntPattern
    contentPageTemplate = "/%s.html"
    content {
        document {
            items("#content > p")
        }
    }
}
}

