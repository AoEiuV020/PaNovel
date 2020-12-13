package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.anull.notNull
import cc.aoeiuv020.base.jar.absSrc
import cc.aoeiuv020.base.jar.textList
import cc.aoeiuv020.base.jar.textListSplitWhitespace
import cc.aoeiuv020.js.JsUtil
import cc.aoeiuv020.okhttp.get
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.regex.compileRegex

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
        synchronized(lock) {
            post {
                // https://www.123ds.org/Search/
                url = "/Search/"
                charset = "GBK"
                data {
                    "q" to it
                }
            }
            val finalCall = call.notNull()
            var retry = false
            var ret = document {
                if (root.getElements("div.DivMargin").isNullOrEmpty()) {
                    retry = checkCookie()
                    novelItemList = emptyList()
                } else {
                    items("div.DivMargin > a.Title") {
                        name(":root")
                        author("div.DivMargin > font:nth-child(${index * 8 + 4})", parent = root.ownerDocument())
                    }
                }
            }
            if (retry) {
                call = finalCall.clone()
                ret = document {
                    items("div.DivMargin > a.Title") {
                        name(":root")
                        author("div.DivMargin > font:nth-child(${index * 8 + 4})", parent = root.ownerDocument())
                    }
                }
            }
            ret
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
        synchronized(lock) {
            call = connect(getNovelContentUrl(extra))
            val finalCall = call.notNull()
            var retry = false
            var ret = document {
                if (root.getElements("div#DivContentBG").isNullOrEmpty()) {
                    retry = checkCookie()
                    novelContent = emptyList()
                } else {
                    parseContent()
                }
            }
            if (retry) {
                call = finalCall.clone()
                ret = document {
                    parseContent()
                }
            }
            ret
        }
    }
}

    private val lock = Any()

    private fun _NovelContentParser.parseContent() {
        // 正文的id是可变的，同时文字的顺序是可能反的，同时p可能是不存在的，
        val div = root.requireElement("div#DivContentBG > div[id]", TAG_CONTENT)
        val js = root.getElements("div#DivContentBG script:not([language])")?.map { it.html() }?.firstOrNull { it.contains("eval") && it.contains("String.fromCharCode") && it.contains(div.id()) }
        novelContent = if (js != null) {
            // 这时候文字是反的，
            div.textList().reversed().map { it.reversed() }
        } else {
            div.textList()
        }
    }

    private fun _Parser<Any>.checkCookie(): Boolean {
        val c2e = root.getElement("script[language=javascript]")
        val js2 = root.getElement("script[type=text/javascript]")?.absSrc()
        if (c2e != null && !js2.isNullOrEmpty()) {
            val js = JsUtil.create()
            js.run(c2e.html())
            var ret2 = responseBody(client.get(js2)).string()
            ret2 = ret2.replaceFirst(compileRegex(".*function ajax"), "function ajax")
            ret2 = ret2.replaceFirst(compileRegex("Ajax.open.*"), "")
            js.run(ret2)
            val path = js.run("ajax(c2);")
            val ret3 = responseBody(client.get(baseHttpUrl.newBuilder().encodedPath(path).build().toString())).string()
            if (ret3 == "ok") {
                return true
            }
        }
        return false
    }
}

