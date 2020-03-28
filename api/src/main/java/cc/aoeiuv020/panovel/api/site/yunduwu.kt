@file:Suppress("UnnecessaryVariable")

package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.absHref
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext

class Yunduwu : DslJsoupNovelContext() {init {
    site {
        name = "云读物"
        baseUrl = "https://www.yunduwu.com"
        logo = "https://www.yunduwu.com/upload/carousel/20191122/157441363654181.jpg"
    }
    search {
        get {
            // https://www.yunduwu.com/novel/search?keywords=%E4%B9%A6
            url = "/novel/search"
            data {
                "keywords" to it
            }
        }
        document {
            items("body > div.g-scrollview > article > a") {
                extra("a")
                name("> div.list-mes > h3")
                author("> div.list-mes > div > div > span:nth-child(1)", block = pickString("作者：(.*)"))
            }
        }
    }
    // https://www.yunduwu.com/book/607
    detailPageTemplate = "/book/%s"
    detail {
        document {
            novel {
                name("div.flex-left > div.padding.grap > div")
                author("div.flex-left > div.padding.grap > p:nth-child(2) > span")
            }
            image("div.flex-left > div.cover > img")
            update("body > section > div > div:nth-child(3) > p:nth-child(1)", format = "最近更新：yyyy-MM-dd HH:mm:ss")
            introduction("p.indent")
        }
    }
    // https://www.yunduwu.com/book/607/contents.html
    chaptersPageTemplate = "/book/%s/contents.html"
    chapters {
        var next: String? = getNovelChapterUrl(it)
        val ret = mutableListOf<NovelChapter>()
        while (next != null) {
            get {
                url = next
            }
            document {
                items("div.contents > ul > li > a") {
                    extra = findBookIdWithChapterId(root.absHref())
                    name("> span")
                }
                // 加载下一页，
                next = root.selectFirst("ul.pagination > li:nth-last-child(1):not(.disabled) > a")?.absHref()
            }.let { ret.addAll(it) }
        }
        ret
    }
    // https://www.yunduwu.com/book/607/3579.html
    contentPageTemplate = "/book/%s.html"
    content {
        document {
            items("div.content > div.txt > div")
        }
    }
}
}

