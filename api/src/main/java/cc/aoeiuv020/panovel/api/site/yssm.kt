package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.pick
import cc.aoeiuv020.panovel.api.*
import org.jsoup.Connection
import org.jsoup.nodes.Document
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/**
 * Created by AoEiuV020 on 2018.05.10-16:48:32.
 */
class Yssm : JsoupNovelContext() {
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
        } ?: Date(0)

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
