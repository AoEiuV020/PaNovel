package cc.aoeiuv020.panovel.api

import org.jsoup.nodes.TextNode
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat

/**
 *
 * Created by AoEiuV020 on 2018.03.14-01:35:36.
 */
class Syxs : NovelContext() {
    companion object {
        private val SEARCH_PAGE_URL = "http://zhannei.baidu.com/cse/search"
    }

    override val charset: String? = "GBK"

    private val site = NovelSite(
            name = "31小说",
            baseUrl = "http://www.31xs.org/",
            logo = "https://imgsa.baidu.com/forum/w%3D580/sign=e8432cdf06d162d985ee621421dfa950/47e082d8bc3eb135d9b1d5a0aa1ea8d3fc1f44a6.jpg"
    )

    override fun getNovelSite(): NovelSite = site

    override fun check(url: String): Boolean {
        return super.check(url)
                || URL(url).host == "www.31xs.com"
                || (isSearchResult(url) && url.contains("7845455592055299828"))
    }

    private fun isSearchResult(url: String): Boolean {
        return url.startsWith(SEARCH_PAGE_URL)
    }

    override fun getGenres(): List<NovelGenre> {
        val root = request(site.baseUrl)
        val elements = root.select("#wrapper > div.nav > ul > li > a").drop(1).dropLast(1)
        return elements.map { a ->
            NovelGenre(a.text(), GenreListRequester(a.absHref()))
        }
    }

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelList(requester: ListRequester): List<NovelListItem> {
        // 搜索是百度的，编码utf-8, 直接null就可以，
        val root = response(requester).charset(if (requester is SearchListRequester) null else charset).parse()
        return if (requester is SearchListRequester) root.select("#results > div.result-list > div > div.result-game-item-detail").map {
            val a = it.select("h3 > a").first()
            val name = a.title()
            val url = a.absHref()
            val author = it.select("> div > p:nth-child(1) > span:nth-child(2)").first().text().trim()
            val genre = it.select("> div > p:nth-child(2) > span:nth-child(2)").first().text()
            val update = it.select("> div > p:nth-child(3) > span:nth-child(2)").first().text()
            val about = it.select(" > p").first().text()
            val info = "类型: $genre 更新: $update 简介: $about"
            NovelListItem(NovelItem(this, name, author, url), info)
        }
        else root.select("#content > table > tbody > tr").drop(1).map {
            val a = it.select("> td:nth-child(1) > a").first()
            val name = a.text()
            val url = a.absHref()
            val author = it.select("> td:nth-child(3)").first().text()
            val last = it.select("> td.even").first().text()
            val number = it.select("> td.center").first().text()
            val info = "最新章节: $last 字数: $number"
            NovelListItem(NovelItem(this, name, author, url), info)
        }
    }

    override fun searchNovelName(name: String): NovelGenre {
        val key = URLEncoder.encode(name, "UTF-8")
        val url = "$SEARCH_PAGE_URL?s=7845455592055299828&q=$key"
        return NovelSearch(name, url)
    }

    override fun getNextPage(genre: NovelGenre): NovelGenre? {
        val query = if (genre.requester is SearchListRequester) {
            "#pageFooter > a.pager-next-foot.n"
        } else {
            "#content > div > a:nth-last-child(1)"
        }
        val root = request(genre.requester)
        val a = root.select(query).first() ?: return null
        val url = a.absHref()
        if (url.isEmpty()) return null
        return if (genre.requester is SearchListRequester) {
            NovelSearch(genre.name, url)
        } else {
            NovelGenre(genre.name, url)
        }
    }

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelDetail(requester: DetailRequester): NovelDetail {
        val root = request(requester)
        val img = root.select("#fmimg > img").first().absSrc()
        val div = root.select("#info").first()
        val name = div.select("> h1").first().text()
        val (author) = div.select("> p:nth-child(2)").first().text()
                .pick("作    者：(\\S*)")
        val introduction = root.select("#intro").first().childNode(0).let { (it as TextNode).wholeText }.trim()

        val updateString = root.select("head > meta[property=og:novel:update_time]").first().attr("content")
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val update = sdf.parse(updateString)

        val chapterPageUrl = requester.url
        return NovelDetail(NovelItem(this, name, author, requester), img, update, introduction, chapterPageUrl)
    }

    override fun getNovelChaptersAsc(requester: ChaptersRequester): List<NovelChapter> {
        val root = request(requester)
        return root.select("#list > dl > dd > a").dropWhile { it.text() != "加入书架" }.drop(1).map { a ->
            NovelChapter(a.text(), a.absHref())
        }
    }

    override fun getNovelText(requester: TextRequester): NovelText {
        val root = request(requester)
        val textList = root.select("#content > p").map {
            it.text().trim()
        }
        return NovelText(textList)
    }
}
