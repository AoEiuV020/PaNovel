package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.pick
import cc.aoeiuv020.panovel.api.*
import org.jsoup.nodes.TextNode
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat

/**
 *
 * Created by AoEiuV020 on 2018.03.14-01:35:36.
 */
class Syxs : JsoupNovelContext() {
    companion object {
        private val SEARCH_PAGE_URL = "http://zhannei.baidu.com/cse/search"
    }

    override val charset: String? = "GBK"

    override val site = NovelSite(
            name = "31小说",
            baseUrl = "http://www.31xs.net/",
            logo = "https://imgsa.baidu.com/forum/w%3D580/sign=e8432cdf06d162d985ee621421dfa950/47e082d8bc3eb135d9b1d5a0aa1ea8d3fc1f44a6.jpg"
    )

    override fun check(url: String): Boolean {
        return super.check(url)
                || URL(url).host.startsWith("www.31xs.")
                || (isSearchResult(url) && url.contains("7845455592055299828"))
    }

    override fun getNovelItem(url: String): NovelItem {
        val path = URL(url).path.removePrefix("/")
        val detailUrl = "${site.baseUrl}$path"
        return super.getNovelItem(detailUrl)
    }

    private fun isSearchResult(url: String): Boolean {
        return url.startsWith(SEARCH_PAGE_URL)
    }

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelList(requester: Requester): List<NovelListItem> {
        // 搜索是百度的，编码utf-8, 直接null就可以，
        val root = response(requester).charset(null).parse()
        return root.select("#results > div.result-list > div > div.result-game-item-detail").map {
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
    }

    override fun searchNovelName(name: String): NovelGenre {
        // TODO: 这网站已经有自己的搜索了，
        val key = URLEncoder.encode(name, "UTF-8")
        val url = "${SEARCH_PAGE_URL}?s=7845455592055299828&q=$key"
        return NovelGenre(name, url)
    }

    override fun getNextPage(genre: NovelGenre): NovelGenre? {
        val query = "#pageFooter > a.pager-next-foot.n"
        val root = request(genre.requester)
        val a = root.select(query).first() ?: return null
        val url = a.absHref()
        if (url.isEmpty()) return null
        return NovelGenre(genre.name, url)
    }

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelDetail(requester: Requester): NovelDetail {
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

    override fun getNovelChaptersAsc(requester: Requester): List<NovelChapter> {
        val root = request(requester)
        return root.select("#list > dl > dd > a").dropWhile { it.text() != "加入书架" }.drop(1).map { a ->
            NovelChapter(a.text(), a.absHref())
        }
    }

    override fun getNovelText(requester: Requester): NovelText {
        val root = request(requester)
        val textList = root.select("#content > p").map {
            it.text().trim()
        }
        return NovelText(textList)
    }
}
