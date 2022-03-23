package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.ownerPath
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstThreeIntPattern
import cc.aoeiuv020.panovel.api.firstTwoIntPattern

/**
 * Created by AoEiuV020 on 2018.06.06-15:22:06.
 */
class Dajiadu : DslJsoupNovelContext() {init {
    site {
        name = "大家读书院"
        baseUrl = "https://www.dajiadu8.com"
        logo = "https://www.dajiadu8.com/17mb/style/logo.png"
    }
    search {
        get {
            // https://www.dajiadu8.com/modules/article/searchsou1.php
            charset = "GBK"
            url = "/modules/article/searchsou1.php"
            data {
                "searchkey" to it
                // 加上&page=1可以避开搜索时间间隔的限制，
                // 也可以通过不加载cookies避开搜索时间间隔的限制，
                "page" to "1"
            }
        }
        document {
            if (root.ownerPath().endsWith("/")) {
                single {
                    name("span.novelname")
                    author("span.novelauthor", block = pickString("作\\s*者：(\\S*)"))
                }
            } else {
                items("ul.list_ul > li") {
                    name("> p.p1 > a")
                    author("> p.p3")
                }
            }
        }
    }
    // https://www.dajiadu8.com/46/46084/
    bookIdRegex = firstTwoIntPattern
    detailPageTemplate = "/%s/"
    detail {
        document {
            novel {
                name("span.novelname")
                author("span.novelauthor", block = pickString("作\\s*者：(\\S*)"))
            }
            image("div.catalog_pic > img")
            introduction("div.catalognovel_intro", block = ownLinesString())
        }
    }
    chapters {
        document {
            items("> ul > li > a", root.select("div.index_listbox > div.listchapter").last())
        }
    }
    // https://www.dajiadu8.com/46/46084/13555452.html
    bookIdWithChapterIdRegex = firstThreeIntPattern
    contentPageTemplate = "/%s.html"
    content {
        document {
            items("div.chapter_content", block = ownLinesSplitWhitespace())
        }
    }
}
}

