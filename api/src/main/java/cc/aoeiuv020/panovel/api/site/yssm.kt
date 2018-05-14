package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.pick
import cc.aoeiuv020.panovel.api.*
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat

/**
 * Created by AoEiuV020 on 2018.05.10-16:48:32.
 */
class Yssm : NovelContext() {
    companion object {
        private val SEARCH_PAGE_URL = "https://www.yssm.org/SearchBook.php"
    }

    private val site = NovelSite(
            name = "幼狮书盟",
            baseUrl = "https://www.yssm.org/",
            logo = "https://www.yssm.org/images/logo.png"
    )

    override fun getNovelSite(): NovelSite = site

    override fun getNovelItem(url: String): NovelItem {
        val path = URL(url).path.removePrefix("/")
        val detailUrl = "${site.baseUrl}$path"
        return super.getNovelItem(detailUrl)
    }

    override fun getGenres(): List<NovelGenre> {
        val root = request(site.baseUrl)
        val elements = root.select("#subnav > div > p > a").drop(2).dropLast(1)
        return elements.map { a ->
            NovelGenre(a.text(), GenreListRequester(a.absHref()))
        }
    }

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelList(requester: ListRequester): List<NovelListItem> {
        val root = request(requester)
        val query = if (requester is SearchListRequester)
            "#container > div.details.list-type > ul > li"
        else
            "#content1 > div > div.details.list-type > ul > li"
        return root.select(query).map {
            val a = it.select("> span.s2 > a").first()
            val name = a.text()
            val url = a.absHref()
            val author = it.select("> span.s3").first().text()
            val last = it.select("> span.s2 > i > a").first().text()
            val genre = it.select("> span.s1").first().text()
            val updateTime = it.select("> span.s4").first().text()
            val status = it.select("> span.s5").first().text()
            val info = "最新章节: $last 类别: $genre 更新时间: $updateTime 状态: $status"
            NovelListItem(NovelItem(this, name, author, url), info)
        }
    }

    override fun searchNovelName(name: String): NovelGenre {
        val key = URLEncoder.encode(name, "UTF-8")
        val url = "${SEARCH_PAGE_URL}?keyword=$key"
        return NovelSearch(name, url)
    }

    override fun getNextPage(genre: NovelGenre): NovelGenre? {
        return null
    }

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelDetail(requester: DetailRequester): NovelDetail {
        val root = request(requester)
        // 这网站小说没有封面，
        val img = "https://www.snwx8.com/modules/article/images/nocover.jpg"
        val div = root.select("#container > div.bookinfo").first()
        val name = div.select("> div > span > h1").first().text()
        val (author) = div.select("> div > span > em").first().text()
                .pick("作者：(\\S*)")
        val introduction = div.select("> p.intro").first().textNodes().joinToString("\n")

        val updateString = div.select("> p.stats > span.fr > i:nth-child(2)").first().text()
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        val update = sdf.parse(updateString)

        val chapterPageUrl = requester.url
        return NovelDetail(NovelItem(this, name, author, requester), img, update, introduction, chapterPageUrl)
    }

    override fun getNovelChaptersAsc(requester: ChaptersRequester): List<NovelChapter> {
        val root = request(requester)
        // 章节数太少的话，前几章会被抛弃，
        return root.select("#main > div > dl > dd > a").drop(12).map { a ->
            NovelChapter(a.text(), a.absHref())
        }
    }

    override fun getNovelText(requester: TextRequester): NovelText {
        val root = request(requester)
        val textList = root.select("#content").first().textList()
        return NovelText(textList)
    }
}
