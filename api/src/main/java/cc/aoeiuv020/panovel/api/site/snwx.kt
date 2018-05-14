package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.pick
import cc.aoeiuv020.panovel.api.*
import org.jsoup.nodes.TextNode
import java.net.URL
import java.net.URLEncoder
import java.util.*

/**
 *
 * Created by AoEiuV020 on 2018.03.07-02:42:57.
 */
class Snwx : NovelContext() {
    companion object {
        private val SEARCH_PAGE_URL = "https://www.snwx8.com/modules/article/search.php"
    }

    private val site = NovelSite(
            name = "少年文学",
            baseUrl = "https://www.snwx8.com/",
            logo = "https://www.snwx8.com/xiaoyi/images/logo.gif"
    )

    override fun getNovelSite(): NovelSite = site

    override fun getNovelItem(url: String): NovelItem {
        val path = URL(url).path
        val detailUrl = "https://www.snwx8.com$path"
        return super.getNovelItem(detailUrl)
    }

    override fun getGenres(): List<NovelGenre> {
        val root = request(site.baseUrl)
        val elements = root.select("#wrapper > div.nav > ul > li > a").drop(2).dropLast(1)
        return elements.map { a ->
            NovelGenre(a.text(), GenreListRequester(a.absHref()))
        }
    }

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelList(requester: ListRequester): List<NovelListItem> {
        val root = request(requester)
        return when {
            requester is SearchListRequester -> root.select("#newscontent > div.l > ul > li").map {
                val a = it.select("> span.s2 > a").first()
                val name = a.text()
                val url = a.absHref()
                val author = it.select("> span.s4").first().text()
                val last = it.select("> span.s3 > a").first().text()
                val update = it.select("> span.s5").first().text()
                val genre = it.select("> span.s1").first().text()
                val info = "最新章节: $last 类型: $genre 更新: $update"
                NovelListItem(NovelItem(this, name, author, url), info)
            }
            requester.url.startsWith("https://www.snwx8.com/quanben/") -> if (requester.url.matches(Regex(".*[/1]"))) {
                // 如果是第一页，右边的推荐也算上，
                root.select("#newscontent > div.r > ul > li").map {
                    val a = it.select("> span.s2 > a").first()
                    val name = a.text()
                    val url = a.absHref()
                    val author = it.select("> span.s5").first().text()
                    val info = ""
                    NovelListItem(NovelItem(this, name, author, url), info)
                }
            } else {
                emptyList()
            } + root.select("#newscontent > div.l > ul > li").map {
                val a = it.select("> span.s2 > a").first()
                val name = a.text()
                val url = a.absHref()
                val author = it.select("> span.s4").first().text()
                val last = it.select("> span.s3 > a").first().text()
                val update = it.select("> span.s5").first().text()
                val genre = it.select("> span.s1").first().text()
                val info = "最新章节: $last 类型: $genre 更新: $update"
                NovelListItem(NovelItem(this, name, author, url), info)
            }
            else -> if (requester.url.endsWith("1.html")) {
                // 如果是第一页，上面和右边的推荐也算上，
                root.select("#hotcontent > div > div").map {
                    val a = it.select("> dl > dt > a").first()
                    val name = a.text()
                    val url = a.absHref()
                    val (author) = it.select("> dl > dt > span").first().text().pick("作者：(\\S*)")
                    val info = it.select("> dl > dd").first().text().trim()
                    NovelListItem(NovelItem(this, name, author, url), info)
                } + root.select("#newscontent > div.r > ul > li").map {
                    val a = it.select("> span.s2 > a").first()
                    val name = a.text()
                    val url = a.absHref()
                    val author = it.select("> span.s5").first().text()
                    val info = ""
                    NovelListItem(NovelItem(this, name, author, url), info)
                }
            } else {
                emptyList()
            } + root.select("#newscontent > div.l > ul > li").map {
                val a = it.select("> span.s2 > a").first()
                val name = a.text()
                val url = a.absHref()
                val author = it.select("> span.s4").first().text()
                val last = it.select("> span.s3 > a").first().text()
                val update = it.select("> span.s5").first().text()
                val info = "最新章节: $last 更新: $update"
                NovelListItem(NovelItem(this, name, author, url), info)
            }
        }
    }

    override fun searchNovelName(name: String): NovelGenre {
        val key = URLEncoder.encode(name, "GBK")
        val url = "${SEARCH_PAGE_URL}?searchkey=$key"
        return NovelSearch(name, url)
    }

    override fun getNextPage(genre: NovelGenre): NovelGenre? {
        if (genre.requester is SearchListRequester) {
            return null
        }
        val root = request(genre.requester)
        val a = root.select("#pagelink > a.next").first() ?: return null
        val url = a.absHref()
        if (url.isEmpty()) return null
        return NovelGenre(genre.name, url)
    }

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelDetail(requester: DetailRequester): NovelDetail {
        val root = request(requester)
        val img = root.select("#fmimg > img").first().src()
        val div = root.select("#info").first()
        val title = div.select("> div.infotitle").first()
        val name = title.select("> h1").first().text()
        val (author) = title.select("> i:nth-child(2)").first().text()
                .pick("作者：(\\S*)")
        val info = div.select("> div.intro").first().childNode(3).let { (it as TextNode).wholeText }.trim()
        val update = Date(0)

        val chapterPageUrl = requester.url
        return NovelDetail(NovelItem(this, name, author, requester), img, update, info, chapterPageUrl)
    }

    override fun getNovelChaptersAsc(requester: ChaptersRequester): List<NovelChapter> {
        val root = request(requester)
        return root.select("#list > dl > dd > a").map { a ->
            NovelChapter(a.text(), a.absHref())
        }
    }

    override fun getNovelText(requester: TextRequester): NovelText {
        val root = request(requester)
        val content = root.select("#BookText").first()
        return NovelText(content.textList())
    }
}