package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.pick
import cc.aoeiuv020.panovel.api.*
import org.jsoup.Connection
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/**
 *
 * Created by AoEiuV020 on 2018.03.06-18:10:46.
 */
class Sfacg : JsoupNovelContext() {

    override val site = NovelSite(
            name = "SF轻小说",
            baseUrl = "http://book.sfacg.com",
            logo = "http://rs.sfacg.com/images/sflogo.gif"
    )

    override fun connectByNovelName(name: String): Connection {
        val key = URLEncoder.encode(name, "UTF-8")
        return connect("http://s.sfacg.com/?Key=$key&S=1&SS=0")
    }

    override fun getSearchResultList(root: Document): List<NovelItem> {
        return root.requireElements("#form1 > table.comic_cover.Height_px22.font_gray.space10px > tbody > tr > td > ul").map {
            val a = it.requireElement("li > strong > a", name = TAG_NOVEL_LINK)
            val name = a.text()
            val bookId = findBookId(a.href())
            val li = it.requireElement("> li:nth-child(2)")
            val size = li.childNodeSize()
            val all = (li.childNode(size - 3) as TextNode).wholeText.trim()
            val (author, _) = all.pick("综合信息： ([^/]*)/(.*)")
            NovelItem(this, name, author, bookId)
        }
    }

    override val detailTemplate: String
        get() = "/Novel/%s/"

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelDetail(root: Document): NovelDetail {
        val img = root.requireElement("#hasTicket > div.left-part > div > div.pic > a > img", TAG_IMAGE) { it.src() }
        val div = root.requireElement("div.wrap > div.d-summary > div.summary-content")
        val name = div.requireElement("> h1 > span.text", TAG_NOVEL_NAME) { it.text() }
        val author = div.requireElement("> div.count-info.clearfix > div.author-info > div.author-name > span", TAG_AUTHOR_NAME) { it.text() }
        val intro = div.getElement("> p") {
            it.textList().joinToString("\n")
        }.toString()

        val update = div.getElements("> div.count-info.clearfix > div.count-detail span") {
            val (updateString) = it[3].text().pick("更新：(.*)")
            val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
            sdf.parse(updateString)
        } ?: Date(0)

        val bookId = findFirstOneInt(root.location())
        return NovelDetail(NovelItem(this, name, author, bookId), img, update, intro, bookId)
    }

    override val chapterTemplate: String?
        get() = "/Novel/%s/MainIndex/"

    override fun getNovelChaptersAsc(root: Document): List<NovelChapter> {
        return root.requireElements("div.story-catalog > div.catalog-list > ul > li > a", TAG_CHAPTER_LINK).map { a ->
            NovelChapter(a.title(), a.path())
        }
    }

    override val chapterIdRegex: Pattern
        get() = firstThreeIntPattern

    override val contentTemplate: String?
        get() = "/Novel/%s/"

    override fun getNovelText(extra: String): NovelText = try {
        super.getNovelText(extra)
    } catch (e: Exception) {
        getNovelText(parse(extra))
    }

    override fun getNovelText(root: Document): NovelText {
        val list = root.requireElement("#ChapterBody", TAG_CONTENT).childNodes().mapNotNull {
            when {
                it is Element && it.tagName() == "p" -> it.text()
                it is TextNode && !it.isBlank -> it.text().trim()
                else -> null
            }
        }
        return NovelText(list)
    }
}
