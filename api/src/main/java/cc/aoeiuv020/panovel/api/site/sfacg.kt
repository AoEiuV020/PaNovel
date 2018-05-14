package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.pick
import cc.aoeiuv020.panovel.api.*
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.regex.Pattern

/**
 *
 * Created by AoEiuV020 on 2018.03.06-18:10:46.
 */
class Sfacg : NovelContext() {
    companion object {
        private val SEARCH_PAGE_URL = "http://s.sfacg.com/"
    }

    private val site = NovelSite(
            name = "SF轻小说",
            baseUrl = "http://book.sfacg.com/",
            logo = "http://rs.sfacg.com/images/sflogo.gif"
    )

    override fun getNovelSite(): NovelSite = site

    override fun getGenres(): List<NovelGenre> {
        val root = request("http://book.sfacg.com/List/")
        val elements = root.select("#typeMenu > a")
        val pattern = Pattern.compile("javascript:redirect\\('tid',(\\d*)\\)", Pattern.DOTALL)
        return listOf(NovelGenre("全部", GenreListRequester("http://book.sfacg.com/List/"))) + elements.map { a ->
            val (sTid) = a.href().pick(pattern)
            val tid = sTid.toInt()
            NovelGenre(a.text(), GenreListRequester("http://book.sfacg.com/List/?tid=$tid"))
        }
    }

    override fun getNextPage(genre: NovelGenre): NovelGenre? {
        if (genre.requester is SearchListRequester) {
            return null
        }
        val root = request(genre.requester)
        val a = root.select("div.bsubcon > div.list_pages > ul > li.pagebarNext > a").first() ?: return null
        val url = a.absHref()
        if (url.isEmpty()) return null
        return NovelGenre(genre.name, url)
    }

    override fun searchNovelName(name: String): NovelGenre {
        val key = URLEncoder.encode(name, "UTF-8")
        val url = "${SEARCH_PAGE_URL}?Key=$key&S=1&SS=0"
        return NovelSearch(name, url)
    }

    private fun isSearchResult(url: String): Boolean {
        return url.startsWith(SEARCH_PAGE_URL)
    }

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelList(requester: ListRequester): List<NovelListItem> {
        val root = request(requester)
        return if (requester is SearchListRequester) root.select("#form1 > table.comic_cover.Height_px22.font_gray.space10px > tbody > tr > td > ul").map {
            val a = it.select("li > strong > a").first()
            val name = a.text()
            val url = a.absHref()
            val li = it.select("> li:nth-child(2)").first()
            val size = li.childNodeSize()
            val all = (li.childNode(size - 3) as TextNode).wholeText.trim()
            val (author, update) = all.pick("综合信息： ([^/]*)/(.*)")
            val about = (li.childNode(size - 1) as TextNode).wholeText.trim()
            val info = "更新时间: $update 简介: $about"
            NovelListItem(NovelItem(this, name, author, url), info)
        } else root.select("div.bsubcon > div.comic_cover.Blue_link3 > ul").map {
            val a = it.select("> li:nth-child(2) > strong > a").first()
            val name = a.text()
            val url = a.absHref()
            val author = it.select("li:nth-last-child(1) > a").first().text().trim()
            val genre = it.select("> li:nth-child(2) > a:nth-child(6)").first().text()
            val about = it.select("> li:nth-child(2)").first().let { it.childNode(it.childNodeSize() - 1) as TextNode }.wholeText.trim()
            val info = "类型: $genre 简介: $about"
            NovelListItem(NovelItem(this, name, author, url), info)
        }
    }

    override fun check(url: String): Boolean {
        return super.check(url)
                || (isSearchResult(url) && url.contains("S=1"))
    }

    override fun getNovelItem(url: String): NovelItem {
        val bookId = findBookId(url)
        val detailUrl = "${site.baseUrl}Novel/$bookId/"
        return super.getNovelItem(detailUrl)
    }

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelDetail(requester: DetailRequester): NovelDetail {
        val root = request(requester)
        val img = root.select("#hasTicket > div.left-part > div > div.pic > a > img").first().src()
        val div = root.select("div.wrap > div.d-summary > div.summary-content").first()
        val name = div.select("> h1 > span.text").first().text()
        val author = div.select("> div.count-info.clearfix > div.author-info > div.author-name > span").first().text()
        val info = div.select("> p").first().textList().joinToString("\n")

        val (updateString) = div.select("> div.count-info.clearfix > div.count-detail span")[3].text()
                .pick("更新：(.*)")
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        val update = sdf.parse(updateString)

        val chapterPageUrl = requester.url + "/MainIndex/"
        return NovelDetail(NovelItem(this, name, author, requester), img, update, info, chapterPageUrl)
    }

    override fun getNovelChaptersAsc(requester: ChaptersRequester): List<NovelChapter> {
        val root = request(requester)
        return root.select("div.story-catalog > div.catalog-list > ul > li > a\n").map { a ->
            NovelChapter(a.title(), a.absHref())
        }
    }

    override fun getNovelText(requester: TextRequester): NovelText {
        val root = request(requester)
        val list = root.select("#ChapterBody").first().childNodes().mapNotNull {
            when {
                it is Element && it.tagName() == "p" -> it.text()
                it is TextNode && !it.isBlank -> it.text().trim()
                else -> null
            }
        }
        return NovelText(list)
    }
}
