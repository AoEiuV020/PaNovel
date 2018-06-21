package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.reverseRemoveDuplication

/**
 * Created by AoEiuV020 on 2018.06.09-18:46:58.
 */
class Kuxiaoshuo : DslJsoupNovelContext() {init {
    site {
        name = "酷小说"
        baseUrl = "https://www.kuxiaoshuo.com"
        logo = "https://raw.githubusercontent.com/AoEiuV020/PaNovel/static/logo/kuxiaoshuo.png"
    }
    search {
        get {
            // https://www.kuxiaoshuo.com/modules/article/search.php?searchkey=%E9%83%BD%E5%B8%82
            url = "/modules/article/search.php"
            data {
                "searchkey" to it
            }
        }
        document {
            items("#hotcontent > table > tbody > tr:not(:nth-child(1))") {
                name("> td:nth-child(1) > a")
                author("> td:nth-child(3)")
            }
        }
    }
    // https://www.kuxiaoshuo.com/15_15368/
    bookIdRegex = "/(\\d+_\\d+)"
    detailPageTemplate = "/%s/"
    detail {
        document {
            novel {
                name("#info > h1")
                author("#info > p:nth-child(2)", block = pickString("作\\s*者：(\\S*)"))
            }
            image("#fmimg > img")
            update("#info > p:nth-child(4)", format = "最后更新：yyyy-MM-dd")
            introduction("#intro > p:nth-child(1)")
        }
    }
    chapters {
        document {
            items("#list > dl > dd > a")
            lastUpdate("#info > p:nth-child(4)", format = "最后更新：yyyy-MM-dd")
        }.reverseRemoveDuplication()
    }
    bookIdWithChapterIdRegex = "/(\\d+_\\d+/\\d+)"
    contentPageTemplate = "/%s.html"
    content {
        document {
            items("#content")
        }
    }
}
}

