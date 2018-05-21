package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.pick
import cc.aoeiuv020.panovel.api.*
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.base.JsoupNovelContext
import org.jsoup.Connection
import org.jsoup.nodes.Document
import java.net.URLEncoder
import java.util.regex.Pattern

/**
 *
 * Created by AoEiuV020 on 2018.03.07-02:42:57.
 */
class Snwx : DslJsoupNovelContext() {init {
    site {
        name = "少年文学"
        baseUrl = "https://www.snwx8.com"
        logo = "https://www.snwx8.com/xiaoyi/images/logo.gif"
    }
    search {
        get {
            url = "/modules/article/search.php?searchkey=${gbk(it)}"
        }
        // 删除cookie绕开搜索时间间隔限制，
        requireNotNull(connection).request().removeCookie("jieqiVisitTime")
        document {
            items("#newscontent > div.l > ul > li") {
                name("> span.s2 > a")
                author("> span.s4")
            }
        }
    }
    bookIdRegex = firstTwoIntPattern
    // https://www.snwx8.com/book/66/66076/
    detailTemplate = "/book/%s/"
    detail {
        document {
            val div = element("#info")
            val title = element("> div.infotitle", parent = div)
            novel {
                name("> h1", parent = title)
                author("> i:nth-child(2)", parent = title) {
                    it.text().pick("作者：(\\S*)").first()
                }
            }
            image("#fmimg > img")
            introduction("> div.intro", parent = div) {
                it.textNodes().first {
                    // TextNode不可避免的有空的，
                    !it.isBlank
                            && !it.wholeText.let {
                        // 后面几个TextNode广告包含这些文字，
                        it.startsWith("各位书友要是觉得《${novel?.name}》还不错的话请不要忘记向您QQ群和微博里的朋友推荐哦！")
                                || it.startsWith("${novel?.name}最新章节,${novel?.name}无弹窗,${novel?.name}全文阅读.")
                    }
                }.ownTextList().joinToString()
            }
            // 这网站详情页没有更新时间，
        }
    }
    chapters {
        document {
            items("#list > dl > dd > a")
        }
    }
    chapterIdRegex = firstThreeIntPattern
    contentTemplate = "/book/%s.html"
    content {
        document {
            items("#BookText")
        }
    }
}
}

// 这类已经不用了，搞定dsl就删除，
@Suppress("ClassName", "unused")
class _Snwx : JsoupNovelContext() {
    override val site = NovelSite(
            name = "少年文学",
            baseUrl = "https://www.snwx8.com",
            logo = "https://www.snwx8.com/xiaoyi/images/logo.gif"
    )

    override fun connectByNovelName(name: String): Connection {
        val key = URLEncoder.encode(name, "GBK")
        return connect(absUrl("/modules/article/search.php?searchkey=$key")).also {
            // 删除cookie绕开搜索时间间隔限制，
            it.request().removeCookie("jieqiVisitTime")
        }
    }

    override fun getSearchResultList(root: Document): List<NovelItem> {
        return root.requireElements("#newscontent > div.l > ul > li").map {
            val a = it.requireElement("> span.s2 > a", TAG_NOVEL_LINK)
            val name = a.text()
            val bookId = findBookId(a.href())
            val author = it.requireElement("> span.s4", TAG_AUTHOR_NAME) { it.text() }
            NovelItem(this, name, author, bookId)
        }
    }

    override val bookIdRegex: Pattern
        get() = firstTwoIntPattern

    // https://www.snwx8.com/book/66/66076/
    override val detailTemplate: String?
        get() = "/book/%s/"

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelDetail(root: Document): NovelDetail {
        val img = root.requireElement("#fmimg > img", TAG_IMAGE) { it.src() }
        val div = root.requireElement("#info")
        val title = div.requireElement("> div.infotitle")
        val name = title.requireElement("> h1", TAG_NOVEL_NAME) { it.text() }
        val author = title.requireElement("> i:nth-child(2)", TAG_AUTHOR_NAME) {
            val (authorString) = it.text().pick("作者：(\\S*)")
            authorString
        }
        val intro = div.getElement(query = "> div.intro") {
            // 只有第一个textNode是简介，其他都是广告文字，
            it.textNodes().first {
                // TextNode不可避免的有空的，
                !it.isBlank
                        && !it.wholeText.let {
                    // 后面几个TextNode广告包含这些文字，
                    it.startsWith("各位书友要是觉得《${name}》还不错的话请不要忘记向您QQ群和微博里的朋友推荐哦！")
                            || it.startsWith("${name}最新章节,${name}无弹窗,${name}全文阅读.")
                }
            }.ownTextList().joinToString()
        }.toString()
        val update = null

        val bookId = findFirstOneInt(root.location())
        return NovelDetail(NovelItem(this, name, author, bookId), img, update, intro, bookId)
    }

    override fun getNovelChaptersAsc(root: Document): List<NovelChapter> {
        return root.requireElements("#list > dl > dd > a", TAG_CHAPTER_LINK).map { a ->
            NovelChapter(a.text(), a.path())
        }
    }

    override val chapterIdRegex: Pattern
        get() = firstThreeIntPattern

    override val contentTemplate: String?
        get() = "/book/%s.html"

    override fun getNovelText(root: Document): NovelText {
        val content = root.requireElement("#BookText")
        return NovelText(content.ownTextList())
    }
}