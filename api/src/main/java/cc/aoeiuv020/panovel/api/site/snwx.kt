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
            baseUrl = "https://www.snwx8.com",
            logo = "https://www.snwx8.com/xiaoyi/images/logo.gif"
    )

    override fun getNovelItem(url: String): NovelItem {
        // 这个path是斜杆/开头的，
        val path = URL(url).path
        val detailUrl = "${site.baseUrl}$path"
        return super.getNovelItem(detailUrl)
    }

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelList(requester: Requester): List<NovelListItem> {
        val root = response(connect(requester)).parse()
        return root.requireElements("#newscontent > div.l > ul > li").map {
            val a = it.requireElement("> span.s2 > a", TAG_NOVEL_LINK)
            val name = a.text()
            val url = a.absHref()
            val author = it.requireElement("> span.s4", TAG_AUTHOR_NAME) { it.text() }
            val last = it.requireElement("> span.s3 > a") { it.text() }
            val update = it.getElement("> span.s5") { it.text() }
            val genre = it.getElement("> span.s1") { it.text() }
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
        val img = root.requireElement("#fmimg > img", TAG_IMAGE) { it.src() }
        val div = root.requireElement("#info")
        val title = div.requireElement("> div.infotitle")
        val name = title.requireElement("> h1", TAG_NOVEL_NAME) { it.text() }
        val author = title.requireElement("> i:nth-child(2)", TAG_AUTHOR_NAME) {
            val (authorString) = it.text().pick("作者：(\\S*)")
            authorString
        }
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
        return root.requireElements("#list > dl > dd > a", TAG_CHAPTER_LINK).map { a ->
            NovelChapter(a.text(), a.absHref())
        }
    }

    override fun getNovelText(requester: Requester): NovelText {
        val root = request(requester)
        val content = root.requireElement("#BookText")
        return NovelText(content.textList())
    }
}