package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.ownTextListSplitWhitespace
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstIntPattern
import cc.aoeiuv020.regex.matches
import cc.aoeiuv020.regex.pick
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by AoEiuV020 on 2018.06.03-21:44:39.
 */
class Aileleba : DslJsoupNovelContext() {init {
    // 网站倒闭了，域名被重定向到另一个网站了，小说id不通用所以不是同一个站，
    hide = true
    site {
        name = "乐安宣书网"
        baseUrl = "http://www.ailelexs.com"
        logo = "https://imgsa.baidu.com/forum/w%3D580/sign=ed8bde7afb03918fd7d13dc2613c264b/86de0c0a304e251f1b666d62ab86c9177d3e5386.jpg"
    }
    search {
        post {
            // http://www.aileleba.com/modules/article/search.php
            // 电脑版搜索页面没有作者名，会出点问题，用手机版代替，
            // http://m.ailelexs.com/s.php
            charset = "GBK"
            url = "//m.ailelexs.com/s.php"
            data {
                "type" to "articlename"
                "s" to it
            }
        }
        document {
            items("body > div.cover > p") {
                name("> a.blue")
                author("p") {
                    it.ownText().removePrefix("/")
                }
            }
        }
    }
    // http://www.aileleba.com/163455.shtml
    bookIdRegex = firstIntPattern
    detailPageTemplate = "/%s.shtml"
    detail {
        document {
            novel {
                name("body > div.wrapper > h1")
                author("body > div.wrapper > div.excerpt", block = pickString("作\\s*者：(\\S*)"))
            }
            image("body > div.wrapper > div.excerpt > img")
            introduction("body > div.wrapper > div.excerpt") {
                it.textNodes()
                        .dropWhile { !it.text().trim().matches("最后更新：\\S+ \\S+") }
                        .also {
                            update = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).parse(it.first().wholeText.trim().pick("最后更新：(\\S+ \\S+)").first())
                        }
                        .drop(1)
                        .flatMap { it.ownTextListSplitWhitespace() }
                        .toMutableList().also { list ->
                            list.lastOrNull()?.removeSuffix("...${root.ownerDocument().location()}")
                                    ?.let {
                                        list[list.lastIndex] = it
                                    }
                        }
                        .joinToString("\n")
            }
        }
    }
    // 这网站电脑版的章节不全，
    // http://m.ailelexs.com/189228.shtml
    chaptersPageTemplate = "//m.ailelexs.com/%s.shtml"
    chapters {
        document {
            items("#chapterList > li > a")
            lastUpdate("div.block_txt2 > p:nth-child(7)", format = "yyyy-MM-dd", block = pickString("更新：(\\S+)"))
        }
    }
    // http://www.aileleba.com/163455/zhangjie38921903.shtml
    bookIdWithChapterIdRegex = "/(\\d+/zhangjie\\d+)"
    contentPageTemplate = "/%s.shtml"
    content {
        document {
            items("#content")
        }.toMutableList().also { list ->
            list.firstOrNull()?.removePrefix("百度搜索乐安宣書網小说稳定更新最快")?.trim()?.let {
                list[0] = it

            }
            list.lastOrNull()?.removeSuffix("百度搜索乐安宣書網(乐安宣书网http://www.aileleba.com)")?.let {
                        list[list.lastIndex] = it
                    }
        }
    }
}
}

