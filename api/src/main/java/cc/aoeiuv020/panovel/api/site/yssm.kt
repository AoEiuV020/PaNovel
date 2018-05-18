package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.pick
import cc.aoeiuv020.panovel.api.*
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by AoEiuV020 on 2018.05.10-16:48:32.
 */
class Yssm : JsoupNovelContext() {
    companion object {
        private val SEARCH_PAGE_URL = "https://www.yssm.org/SearchBook.php"
    }

    override val site = NovelSite(
            name = "幼狮书盟",
            baseUrl = "https://www.yssm.org/",
            logo = "https://www.yssm.org/images/logo.png"
    )

    override fun getNovelItem(url: String): NovelItem {
        val path = URL(url).path.removePrefix("/")
        val detailUrl = "${site.baseUrl}$path"
        return super.getNovelItem(detailUrl)
    }

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelList(requester: Requester): List<NovelListItem> {
        // 傻哔吧这网站，一次性返回所有，搜索都市直接出四千多结果，html大于1M，
        // 这里限制一下，20K大概小几十个结果，
        val root = response(connect(requester).maxBodySize(1000 * 20)).parse()
        // 由于被截断，可能处理最后一个元素会出异常，无视，
        return root.requireElements("#container > div.details.list-type > ul > li").mapIgnoreException {
            val a = it.requireElement(query = "> span.s2 > a", name = TAG_NOVEL_LINK)
            val name = a.text()
            val url = a.absHref()
            val author = it.requireElement(query = "> span.s3", name = TAG_AUTHOR_NAME) { it.text() }
            val last = it.getElement(query = "> span.s2 > i > a") {
                it.text()
            }
            val genre = it.getElement(query = "> span.s1") { it.text() }
            val updateTime = it.getElement(query = "> span.s4") { it.text() }
            val status = it.getElement(query = "> span.s5") { it.text() }
            val info = "最新章节: $last 类别: $genre 更新时间: $updateTime 状态: $status"
            NovelListItem(NovelItem(this, name, author, url), info)
        }
    }

    override fun searchNovelName(name: String): NovelGenre {
        val key = URLEncoder.encode(name, "UTF-8")
        val url = "${SEARCH_PAGE_URL}?keyword=$key"
        return NovelGenre(name, url)
    }

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelDetail(requester: Requester): NovelDetail {
        val root = request(requester)
        // 这网站小说没有封面，
        val img = "https://www.snwx8.com/modules/article/images/nocover.jpg"
        val div = root.requireElement("#container > div.bookinfo")
        val name = div.requireElement("> div > span > h1") { it.text() }
        val author = div.requireElement("> div > span > em") {
            val (author) = it.text().pick("作者：(\\S*)")
            author
        }
        val introduction = div.getElement("> p.intro") {
            it.textNodes().joinToString("\n")
        }.toString()

        val update = div.getElement("> p.stats > span.fr > i:nth-child(2)") {
            val updateString = it.text()
            val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
            sdf.parse(updateString)
        } ?: Date(0)

        val chapterPageUrl = requester.url
        return NovelDetail(NovelItem(this, name, author, requester), img, update, introduction, chapterPageUrl)
    }

    override fun getNovelChaptersAsc(requester: Requester): List<NovelChapter> {
        val root = request(requester)
        // 章节数太少的话，前几章会被抛弃，
        return root.requireElements("#main > div > dl > dd > a", TAG_CHAPTER_LINK).drop(12).map { a ->
            NovelChapter(a.text(), a.absHref())
        }
    }

    override fun getNovelText(requester: Requester): NovelText {
        val root = request(requester)
        val textList = root.requireElements("#content", TAG_CONTENT).first().textList()
        return NovelText(textList)
    }
}
