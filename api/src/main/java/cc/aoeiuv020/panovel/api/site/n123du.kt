package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.anull.notNull
import cc.aoeiuv020.base.jar.*
import cc.aoeiuv020.js.JsUtil
import cc.aoeiuv020.okhttp.get
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.regex.compileRegex
import cc.aoeiuv020.string.lastDivide
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode

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
                // https://www.123ds.org/sscc/
                url = "/sscc/"
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
                    val div = root.requireElement("div.DivMainLeft > div.DivBoder > div.DivMargin")
                    var status = -1
                    var name = ""
                    var extra = ""
                    novelItemList = div.childNodes().mapNotNull { childNode ->
                        when (status) {
                            -1 -> {
                                if (childNode is TextNode) {
                                    if (childNode.text().contains("小说")) {
                                        status = 1
                                    } else if (childNode.text().contains("作者")) {
                                        status = 2
                                    }
                                }
                            }
                            1 -> {
                                val ele = childNode as Element
                                name = ele.text()
                                extra = findBookId(ele.href())
                                status = -1
                            }
                            2 -> {
                                val ele = childNode as Element
                                val author = ele.text()
                                status = -1
                                return@mapNotNull NovelItem(site.name, name, author, extra)
                            }
                        }
                        return@mapNotNull null
                    }
                }
            }
            if (retry) {
                call = finalCall.clone()
                ret = document {
                    items("div.DivMargin > a.Title") {
                        name(":root")
                        author(
                            "div.DivMargin > font:nth-child(${index * 8 + 4})",
                            parent = root.ownerDocument()
                        )
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
                author(
                    "div.DivBoder > div:nth-child(3) > span:nth-child(2)",
                    block = pickString("作\\s*者：(\\S*)")
                )
            }
            image("div.DivImage > center > img")
            introduction = root.getElements("div > div.DivBigIntro") { elements ->
                elements.joinToString("\n") {
                    it.textListSplitWhitespace().joinToString("\n")
                }
            }
            update(
                "div.DivBoder > div:nth-child(3) > span[style]",
                format = "yyyy-MM-dd HH:mm:ss",
                block = pickString("更新：(.*)")
            )
        }
    }
    // https://www.123ds.org/dudu-40/705684/list/
    chaptersPageTemplate = "/dudu-%s/list/"
    chapters { _ ->
        document {
            items(
                root.requireElements("#DivTitleList > div", name = TAG_CHAPTER_LINK)
                    .flatMap { element ->
                        element.requireElements("> span > a", name = TAG_CHAPTER_LINK).reversed()
                    })
            lastUpdate(
                "div.DivMain > div:nth-child(2) > span[style]",
                format = "yyyy-MM-dd HH:mm:ss",
                block = pickString("更新：(.*)")
            )
        }.let { list ->
            var cacheExtra: String? = null
            list.map { novelChapter ->
                val nextIndex = cacheExtra?.let { previousExtra ->
                    if (previousExtra.startsWith(novelChapter.extra)) {
                        val lastIndex = try {
                            previousExtra.lastDivide(':').second.toInt()
                        } catch (e: Exception) {
                            0
                        }
                        lastIndex + 1
                    } else {
                        0
                    }
                } ?: 0

                NovelChapter(
                    novelChapter.name,
                    "${novelChapter.extra}:${nextIndex}".also { cacheExtra = it },
                    novelChapter.update
                )
            }
        }
    }
    // https://www.123ds.org/dudu-40/705684/36287633.html
    bookIdWithChapterIdRegex = "/dudu-(\\d+/\\d+/\\d+)"
    contentPageTemplate = "/dudu-%s.html"
    getNovelContentUrl { extra ->
        contentPageTemplate.notNull().format(extra.lastDivide(':').first)
    }
    content {
        synchronized(lock) {
            var index = extra.lastDivide(':').second.toInt()
            var chapterUrl: String = getNovelContentUrl(extra)
            while (index > 0) {
                call = connect(chapterUrl)
                chapterUrl = this.checkAndParse {
                    root.getElements("li > a").notNull().first { it.html().startsWith("下一章：") }
                        .absHref()
                }.notNull("pageUrl")
                index--
            }
            var next: String? = chapterUrl
            val ret = mutableListOf<String>()
            while (next != null) {
                // 直接connect可能出现正文不全，可能是某个header导致的，
                call = client.get(next)
                this.checkAndParse {
                    // 正文的id是可变的，同时文字的顺序是可能反的，同时p可能是不存在的，
                    val div = root.requireElement("div#DivContentBG > div[id]", TAG_CONTENT)
                    val js = root.getElements("div#DivContentBG script:not([language])")
                        ?.map { it.html() }?.firstOrNull {
                            it.contains("eval") && it.contains("String.fromCharCode") && it.contains(
                                div.id()
                            )
                        }
                    next = root.getElement("#PageSet > a:nth-last-child(1)")?.absHref()
                    if (js != null) {
                        // 这时候文字是反的，
                        div.textList().reversed().map { it.reversed() }
                    } else {
                        div.textList()
                    }.also { novelContent = it }
                }.notNull("content").also { ret.addAll(it) }
            }
            ret
        }
    }
}

    private val lock = Any()

    private fun <T> _Content.checkAndParse(parser: _NovelContentParser.() -> T): T? {
        val finalCall = call.notNull()
        var retry = false
        var ret: T? = null
        document {
            novelContent = emptyList()
            if (root.getElements("div#DivContentBG").isNullOrEmpty()) {
                retry = checkCookie()
            } else {
                ret = parser()
            }
        }
        if (retry) {
            call = finalCall.clone()
            document {
                novelContent = emptyList()
                ret = parser()
            }
        }
        return ret
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
            val ret3 = responseBody(
                client.get(
                    baseHttpUrl.newBuilder().encodedPath(path).build().toString()
                )
            ).string()
            if (ret3 == "ok") {
                return true
            }
        }
        return false
    }
}

