package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.pick
import cc.aoeiuv020.panovel.api.*
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.base.JsoupNovelContext
import org.jsoup.Connection
import org.jsoup.nodes.Document
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.regex.Pattern

/**
 * Created by AoEiuV020 on 2018.05.10-16:48:32.
 */
class Yssm : DslJsoupNovelContext() {init {
    site {
        name = "幼狮书盟"
        baseUrl = "https://www.yssm.org"
        logo = "https://www.yssm.org/images/logo.png"
    }
    search {
        get {
            url = "/SearchBook.php?keyword=$it"
        }
        // 傻哔吧这网站，一次性返回所有，搜索都市直接出四千多结果，html大于1M，
        // 这里限制一下，20K大概小几十个结果，
        requireNotNull(connection).maxBodySize(1000 * 20)
        document {
            // 由于被截断，可能处理最后一个元素会出异常，无视，
            items("#container > div.details.list-type > ul > li") {
                name("> span.s2 > a")
                author("> span.s3")
            }
        }
    }
    bookIdRegex = firstTwoIntPattern
    // https://www.yssm.org/uctxt/227/227934/
    detailTemplate = "/uctxt/%s/"
    detail {
        document {
            val div = root.requireElement("#container > div.bookinfo")
            novel {
                name("> div > span > h1", parent = div)
                author("> div > span > em", parent = div, block = pickString("作者：(\\S*)"))
            }
            // 这网站小说没有封面，
            image = "https://www.snwx8.com/modules/article/images/nocover.jpg"
            introduction("> p.intro", parent = div) {
                it.ownTextList().joinToString("\n")
            }
            update("> p.stats > span.fr > i:nth-child(2)", parent = div, format = "yyyy/MM/dd HH:mm:ss")
        }
    }
    chapters {
        // 章节数太少的话，没有开头的叫最新章节的12章，
        // 这里判断是大于12认为有那12章，扔掉，
        // 并不知道有没有例外，
        // 倒序删除，
        // TODO: 这种情况还真不少，再来一次就抽象出来，
        val list = document {
            items("#main > div > dl > dd > a")
        }
        var index = 0
        // 以防万一，
        if (list.size == 1) return@chapters list
        // 倒序列表判断是否重复章节，
        val reversedList = list.asReversed()
        list.dropWhile {
            (it == reversedList[index]
                    || it.extra.isBlank()).also { ++index }
        }
    }
    chapterIdRegex = firstThreeIntPattern
    // https://www.yssm.org/uctxt/227/227934/1301112.html
    contentTemplate = "/uctxt/%s.html"
    content {
        document {
            items("#content")
        }
    }
}
}

// 这类已经不用了，搞定dsl就删除，
@Suppress("ClassName", "unused")
class _Yssm : JsoupNovelContext() {
    override val site = NovelSite(
            name = "幼狮书盟",
            baseUrl = "https://www.yssm.org",
            logo = "https://www.yssm.org/images/logo.png"
    )

    override fun connectByNovelName(name: String): Connection {
        val key = URLEncoder.encode(name, "UTF-8")
        // 傻哔吧这网站，一次性返回所有，搜索都市直接出四千多结果，html大于1M，
        // 这里限制一下，20K大概小几十个结果，
        return connect(absUrl("/SearchBook.php?keyword=$key")).maxBodySize(1000 * 20)
    }

    override fun getSearchResultList(root: Document): List<NovelItem> {
        // 由于被截断，可能处理最后一个元素会出异常，无视，
        return root.requireElements("#container > div.details.list-type > ul > li").mapIgnoreException {
            val a = it.requireElement(query = "> span.s2 > a", name = TAG_NOVEL_LINK)
            val name = a.text()
            val bookId = findBookId(a.href())
            val author = it.requireElement(query = "> span.s3", name = TAG_AUTHOR_NAME) { it.text() }
            NovelItem(this, name, author, bookId)
        }
    }

    override val bookIdRegex: Pattern
        get() = firstTwoIntPattern

    // https://www.yssm.org/uctxt/227/227934/
    override val detailTemplate: String?
        get() = "/uctxt/%s/"

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelDetail(root: Document): NovelDetail {
        // 这网站小说没有封面，
        val img = "https://www.snwx8.com/modules/article/images/nocover.jpg"
        val div = root.requireElement("#container > div.bookinfo")
        val name = div.requireElement("> div > span > h1") { it.text() }
        val author = div.requireElement("> div > span > em") {
            val (author) = it.text().pick("作者：(\\S*)")
            author
        }
        val intro = div.getElement("> p.intro") {
            it.ownTextList().joinToString("\n")
        }.toString()

        val update = div.getElement("> p.stats > span.fr > i:nth-child(2)") {
            val updateString = it.text()
            val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
            sdf.parse(updateString)
        }

        val bookId = findFirstOneInt(root.location())
        return NovelDetail(NovelItem(this, name, author, bookId), img, update, intro, bookId)
    }

    override fun getNovelChaptersAsc(root: Document): List<NovelChapter> {
        // 章节数太少的话，没有开头的叫最新章节的12章，
        // 这里判断是大于12认为有那12章，扔掉，
        // 并不知道有没有例外，
        return root.requireElements("#main > div > dl > dd > a", TAG_CHAPTER_LINK)
                .let {
                    if (it.size > 12) it.drop(12) else it
                }.map { a ->
                    NovelChapter(a.text(), a.path())
                }
    }

    override val chapterIdRegex: Pattern
        get() = firstThreeIntPattern

    // https://www.yssm.org/uctxt/227/227934/1301112.html
    override val contentTemplate: String?
        get() = "/uctxt/%s.html"

    override fun getNovelText(root: Document): NovelText {
        return NovelText(root.requireElement("#content", TAG_CONTENT).ownTextList())
    }
}
