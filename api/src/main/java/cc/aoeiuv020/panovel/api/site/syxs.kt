package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.pick
import cc.aoeiuv020.panovel.api.*
import org.jsoup.Connection
import org.jsoup.nodes.Document
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.regex.Pattern

/**
 *
 * Created by AoEiuV020 on 2018.03.14-01:35:36.
 */
class Syxs : JsoupNovelContext() {
    companion object {
        private val SEARCH_PAGE_URL = "http://zhannei.baidu.com/cse/search"
    }

    /**
     * 这网站页面没有编码，要自己指定，
     */
    override val charset: String? = "GBK"

    override val site = NovelSite(
            name = "31小说",
            baseUrl = "http://www.31xs.net",
            logo = "https://imgsa.baidu.com/forum/w%3D580/sign=e8432cdf06d162d985ee621421dfa950/47e082d8bc3eb135d9b1d5a0aa1ea8d3fc1f44a6.jpg"
    )

    override fun check(url: String): Boolean {
        return super.check(url)
                || URL(url).host.startsWith("www.31xs.")
                || (isSearchResult(url) && url.contains("7845455592055299828"))
    }

    private fun isSearchResult(url: String): Boolean {
        return url.startsWith(SEARCH_PAGE_URL)
    }

    override fun connectByNovelName(name: String): Connection {
        // TODO: 这网站已经有自己的搜索了，
        val key = URLEncoder.encode(name, "UTF-8")
        return connect("$SEARCH_PAGE_URL?s=7845455592055299828&q=$key")
    }

    override fun searchNovelName(name: String): List<NovelItem> {
        // 搜索是百度的，搜索结果页面是UTF-8,
        return getSearchResultList(parse(connectByNovelName(name), charset = "UTF-8"))
    }

    override fun getSearchResultList(root: Document): List<NovelItem> {
        return root.requireElements("#results > div.result-list > div > div.result-game-item-detail").map {
            val a = it.requireElement("h3 > a", TAG_NOVEL_LINK)
            val name = a.title()
            val bookId = findBookId(a.href())
            val author = it.requireElement("> div > p:nth-child(1) > span:nth-child(2)") { it.text().trim() }
            NovelItem(this, name, author, bookId)
        }
    }

    override val bookIdRegex: Pattern
        get() = firstTwoIntPattern

    // http://www.31xs.net/13/13011/
    override val detailTemplate: String?
        get() = "/%s/"

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelDetail(root: Document): NovelDetail {
        val img = root.requireElement("#fmimg > img", TAG_IMAGE) { it.absSrc() }
        val div = root.requireElement("#info")
        val name = div.requireElement("> h1", TAG_NOVEL_NAME) { it.text() }
        val (author) = div.requireElement("> p:nth-child(2)", TAG_AUTHOR_NAME) { it.text() }
                .pick("作    者：(\\S*)")
        val intro = root.requireElement("#intro") {
            it.ownTextList().joinToString("\n")
        }.toString()
        val update = root.getElement("head > meta[property=og:novel:update_time]") {
            val updateString = it.attr("content")
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            sdf.parse(updateString)
        }

        val bookId = findFirstOneInt(root.location())
        return NovelDetail(NovelItem(this, name, author, bookId), img, update, intro, bookId)
    }

    override fun getNovelChaptersAsc(root: Document): List<NovelChapter> {
        return root.requireElements("#list > dl > dd > a", TAG_CHAPTER_LINK).dropWhile { it.text() != "加入书架" }.drop(1).map { a ->
            NovelChapter(a.text(), a.path())
        }
    }

    override val chapterIdRegex: Pattern
        get() = firstThreeIntPattern

    // http://www.31xs.net/13/13011/8981866.html
    override val contentTemplate: String?
        get() = "/%s.html"

    override fun getNovelText(root: Document): NovelText {
        return NovelText(root.requireElements("#content > p", TAG_CONTENT).ownTextList())
    }
}
