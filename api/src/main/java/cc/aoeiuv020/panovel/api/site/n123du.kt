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
                    retry = checkCookie("www.123ds.org")
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
                    "div.DivBoder > div:nth-child(3) > span:nth-child(1)",
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
                block = pickString("更新时间：(.*)")
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
                "body > div.DivMain > div:nth-child(2) > span[style]",
                format = "yyyy-MM-dd HH:mm:ss",
                block = pickString("更新时间：(.*)")
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
    // https://m.123ds.org/dudu-36/960684/47803707-5.html
    bookIdWithChapterIdRegex = "/dudu-(\\d+/\\d+/\\d+)"
    contentPageTemplate = "//m.123ds.org/dudu-%s.html"
    getNovelContentUrl { extra ->
        contentPageTemplate.notNull().format(extra.lastDivide(':').first)
    }
    content {
        synchronized(lock) {
            var index = extra.lastDivide(':').second.toInt()
            var chapterUrl: String = getNovelContentUrl(extra)
            while (index > 0) {
                call = connect(chapterUrl)
                header { userAgent = defaultUserAgentMobile }
                chapterUrl = this.checkAndParse {
                    root.getElements("div.NextVolume > div > a").notNull().first { it.html().startsWith("下一章") }
                        .absHref()
                }.notNull("pageUrl")
                index--
            }
            var next: String? = chapterUrl
            val ret = mutableListOf<String>()
            while (next != null) {
                // 直接connect可能出现正文不全，可能是某个header导致的，
                call = client.get(next)
                header { userAgent = defaultUserAgentMobile }
                this.checkAndParse {
                    // 改用手机版页面，没有倒序问题，
                    val div = root.requireElement("div#txtuup", TAG_CONTENT)
                    next = root.getElement("#PageSet > a:nth-last-child(1)")?.absHref()
                    div.textList().dropLastWhile {
                        it.contains("提醒您：看完记得收藏")
                                || it.startsWith("本章没完，")
                                || it.startsWith("本章未完，")
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
            if (root.getElements("div#txtuup").isNullOrEmpty()) {
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

    private fun _Parser<Any>.checkCookie(host: String = "m.123ds.org"): Boolean {
        val c2e = root.getElements("script[language=javascript]")?.last()
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
                    baseHttpUrl.newBuilder().host(host).encodedPath(path).build().toString()
                )
            ).string()
            if (ret3 == "ok") {
                return true
            }
        }
        return false
    }
}

