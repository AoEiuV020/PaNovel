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
class Snwx : JsoupNovelContext() {
    companion object {
        private val SEARCH_PAGE_URL = "https://www.snwx8.com/modules/article/search.php"
    }

    override val site = NovelSite(
            name = "少年文学",
            baseUrl = "https://www.snwx8.com/",
            logo = "https://www.snwx8.com/xiaoyi/images/logo.gif"
    )

    override fun getNovelItem(url: String): NovelItem {
        val path = URL(url).path.removePrefix("/")
        val detailUrl = "${site.baseUrl}$path"
        return super.getNovelItem(detailUrl)
    }

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelList(requester: Requester): List<NovelListItem> {
        val root = response(connect(requester)).parse()
        return root.select("#newscontent > div.l > ul > li").map {
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
    }

    override fun searchNovelName(name: String): NovelGenre {
        val key = URLEncoder.encode(name, "GBK")
        val url = "${SEARCH_PAGE_URL}?searchkey=$key"
        return NovelGenre(name, url)
    }

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelDetail(requester: Requester): NovelDetail {
        val root = request(requester)
        val img = root.select("#fmimg > img").first().src()
        val div = root.select("#info").first()
        val title = div.select("> div.infotitle").first()
        val name = title.select("> h1").first().text()
        val (author) = title.select("> i:nth-child(2)").first().text()
                .pick("作者：(\\S*)")
        val intro = div.getElement(query = "> div.intro") {
            it.childNodes().first {
                it is TextNode && !it.isBlank
            }.let { (it as TextNode).wholeText }.trim()
        }.toString()
        val update = Date(0)

        val chapterPageUrl = requester.url
        return NovelDetail(NovelItem(this, name, author, requester), img, update, intro, chapterPageUrl)
    }

    override fun getNovelChaptersAsc(requester: Requester): List<NovelChapter> {
        val root = request(requester)
        return root.select("#list > dl > dd > a").map { a ->
            NovelChapter(a.text(), a.absHref())
        }
    }

    override fun getNovelText(requester: Requester): NovelText {
        val root = request(requester)
        val content = root.select("#BookText").first()
        return NovelText(content.textList())
    }
}