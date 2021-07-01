@file:Suppress("UnnecessaryVariable")

package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.reverseRemoveDuplication

class Biquge5200 : DslJsoupNovelContext() {init {
    site {
        name = "笔趣阁5200ge"
        baseUrl = "https://www.biquge5200.com"
        logo =
            "http://tiebapic.baidu.com/forum/w%3D580/sign=5c4ce951d6d4b31cf03c94b3b7d7276f/27140225bc315c60220999fdd0b1cb134854776b.jpg"
    }
    // https://www.biquge5200.com/modules/article/search.php?searchkey=%E9%83%BD%E5%B8%82
    search {
        get {
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
    // https://www.biquge5200.com/143_143123/
    bookIdRegex = "_(\\d+)"
    detailDivision = 1000
    chapterDivision = detailDivision
    detailPageTemplate = "/%d_%s/"
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
            items("div#list > dl > dd > a")
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

