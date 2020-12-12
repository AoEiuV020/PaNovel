package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.anull.notNull
import cc.aoeiuv020.base.jar.textListSplitWhitespace
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.regex.compilePattern

/**
 *
 * Created by AoEiuV020 on 2018.03.14-01:35:36.
 */
class N123du : DslJsoupNovelContext() {init {
    // 这网站有点坑，故意反爬虫的吧，可能存在没踩到的坑，
    site {
        name = "123读书网"
        baseUrl = "https://www.123ds.org"
        logo = "https://www.123ds.org/SiteFiles/images/NavBG.Gif"
    }
    search {
        post {
            // https://www.123ds.org/Search/
            url = "/Search/"
            charset = "GBK"
            data {
                "q" to it
            }
        }
        document {
            items("div.DivMargin > a.Title") {
                name(":root")
                author("div.DivMargin > font:nth-child(${index * 8 + 4})", parent = root.ownerDocument())
            }
        }
    }
    // https://www.123ds.org/dudu-40/705684/
    bookIdRegex = "/dudu-(\\d+/\\d+)"
    detailPageTemplate = "/dudu-%s/"
    detail { _ ->
        document {
            novel {
                name("div.DivMainLeft > div > h1")
                author("div.DivBoder > div:nth-child(3) > span:nth-child(2)", block = pickString("作\\s*者：(\\S*)"))
            }
            image("div.DivImage > center > img")
            introduction = root.getElements("div > div.DivBigIntro") { elements ->
                elements.joinToString("\n") {
                    it.textListSplitWhitespace().joinToString("\n")
                }
            }
            update("div.DivBoder > div:nth-child(3) > span[style]", format = "yyyy-MM-dd HH:mm:ss", block = pickString("更新：(.*)"))
        }
    }
    // https://www.123ds.org/dudu-40/705684/list/
    chaptersPageTemplate = "/dudu-%s/list/"
    chapters { _ ->
        document {
            items(root.requireElements("#DivTitleList > div", name = TAG_CHAPTER_LINK).flatMap { element ->
                element.requireElements("> span > a", name = TAG_CHAPTER_LINK).reversed()
            })
            lastUpdate("div.DivMain > div:nth-child(2) > span[style]", format = "yyyy-MM-dd HH:mm:ss", block = pickString("更新：(.*)"))
        }
    }
    // https://www.123ds.org/dudu-40/705684/36287633.html
    bookIdWithChapterIdRegex = "/dudu-(\\d+/\\d+/\\d+)"
    contentPageTemplate = "/dudu-%s.html"
    content {
        document {
            // 正文的id是可变的，
            items("div#DivContentBG > div > p")
        }
    }
    cookieFilter {
        // 必须要有这个cookie才能搜索，不知道有效期，需要动态获取js再请求cookie, 可以浏览器搜索一次先，
        removeAll {
            it.name() != "nxgmnmry"
        }
        if (!contains("nxgmnmry")) {
            if (httpUrl.toString().contains("/Search/")) {
                put("nxgmnmry=6d28ae85fcb92a08")
            } else if (compilePattern(".*/dudu-.*.html".notNull()).matcher(httpUrl.toString()).matches()) {
                put("nxgmnmry=45be5760a98afb5a")
            }
        }
    }
}
}

