package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.pick
import cc.aoeiuv020.panovel.api.*
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

/**
 *
 * Created by AoEiuV020 on 2018.03.06-18:10:46.
 */
class Sfacg : JsoupNovelContext() {
    companion object {
        private val SEARCH_PAGE_URL = "http://s.sfacg.com/"
    }

    override val site = NovelSite(
            name = "SF轻小说",
            baseUrl = "http://book.sfacg.com",
            logo = "http://rs.sfacg.com/images/sflogo.gif"
    )

    override fun searchNovelName(name: String): NovelGenre {
        val key = URLEncoder.encode(name, "UTF-8")
        val url = "${SEARCH_PAGE_URL}?Key=$key&S=1&SS=0"
        return NovelGenre(name, url)
    }

    private fun isSearchResult(url: String): Boolean {
        return url.startsWith(SEARCH_PAGE_URL)
    }

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelList(requester: Requester): List<NovelListItem> {
        val root = request(requester)
        return root.requireElements("#form1 > table.comic_cover.Height_px22.font_gray.space10px > tbody > tr > td > ul").map {
            val a = it.requireElement("li > strong > a", name = TAG_NOVEL_LINK)
            val name = a.text()
            val url = a.absHref()
            val li = it.requireElement("> li:nth-child(2)")
            val size = li.childNodeSize()
            val all = (li.childNode(size - 3) as TextNode).wholeText.trim()
            val (author, update) = all.pick("综合信息： ([^/]*)/(.*)")
            val about = (li.childNode(size - 1) as TextNode).wholeText.trim()
            val info = "更新时间: $update 简介: $about"
            NovelListItem(NovelItem(this, name, author, url), info)
        }
    }

    override fun check(url: String): Boolean {
        return super.check(url)
                || (isSearchResult(url) && url.contains("S=1"))
    }

    override fun getNovelItem(url: String): NovelItem {
        val bookId = findBookId(url)
        val detailUrl = "${site.baseUrl}/Novel/$bookId/"
        return super.getNovelItem(detailUrl)
    }

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelDetail(requester: Requester): NovelDetail {
        val root = request(requester)
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

        val chapterPageUrl = requester.url + "/MainIndex/"
        return NovelDetail(NovelItem(this, name, author, requester), img, update, intro, chapterPageUrl)
    }

    override fun getNovelChaptersAsc(requester: Requester): List<NovelChapter> {
        val root = request(requester)
        return root.requireElements("div.story-catalog > div.catalog-list > ul > li > a", TAG_CHAPTER_LINK).map { a ->
            NovelChapter(a.title(), a.absHref())
        }
    }

    override fun getNovelText(requester: Requester): NovelText {
        val root = request(requester)
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
