package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.*
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.*

/**
 *
 * Created by AoEiuV020 on 2017.10.11-19:39:27.
 */
class Liudatxt : JsoupNovelContext() {
    companion object {
        private val SEARCH_PAGE_URL = "http://www.liudatxt.com/search.php"
    }

    override val site = NovelSite(
            name = "溜达小说",
            baseUrl = "http://www.liudatxt.com",
            logo = "https://imgsa.baidu.com/forum/w%3D580/sign=1b4c19b5f0edab6474724dc8c737af81/4afa9ae93901213f074d29a25fe736d12e2e95b9.jpg"
    )

    override fun getNovelItem(url: String): NovelItem {
        val bookId = findBookId(url)
        val detailUrl = "${site.baseUrl}/so/$bookId/"
        return super.getNovelItem(detailUrl)
    }

    override fun getNextPage(genre: NovelGenre): NovelGenre? {
        val root = request(genre.requester)
        return root.getElement(query = "#main > div.list_center > div.pages > a:contains(下一页)") {
            val url = it.absHref()
            if (url.isEmpty()) null
            else NovelGenre(genre.name, url)
        }
    }

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelList(requester: Requester): List<NovelListItem> {
        val root = request(requester)
        return root.requireElements(query = "#sitembox > dl").map {
            val a = it.requireElement(query = "> dd:nth-child(2) > h3 > a", name = TAG_NOVEL_LINK)
            val name = a.text()
            val url = a.absHref()
            val dd3 = it.requireElement(query = "> dd:nth-child(3)")
            val author = dd3.requireElement(query = "> span:nth-child(1)", name = TAG_AUTHOR_NAME) { it.text() }
            val status = dd3.getElement(query = "> span:nth-child(2)") { it.text() }
            val genre = dd3.getElement(query = "> span:nth-child(3)") { it.text() }
            val last = it.getElement(query = "> dd:nth-child(5) > a") { it.text().trim() }
            val about = it.getElement(query = "> dd.book_des") { it.text() }
            val info = run {
                val length = dd3.select("> span:nth-child(4)").first().text()
                val update = it.select("> dd:nth-child(5) > span").first().text()
                "最新章节: $last 类型: $genre 更新: $update 状态: $status 长度: $length 简介: $about"
            }
            NovelListItem(NovelItem(this, name, author, url), info)
        }
    }

    override fun searchNovelName(name: String): NovelGenre {
        return NovelGenre(name, SearchRequester(name))
    }

    class SearchRequester(private val name: String) : Requester(name) {
        override val url = SEARCH_PAGE_URL
        override fun connect(): Connection {
            return Jsoup.connect(SEARCH_PAGE_URL).data("searchkey", name).method(Connection.Method.POST)
        }
    }

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelDetail(requester: Requester): NovelDetail {
        val chapterRoot = request(requester)
        val detail = chapterRoot.requireElement(query = "#main > div.coverecom > div.tabstit > table > tbody > tr > td:nth-child(1) > a:nth-child(4)")
        val root = request(detail.absHref())
        val img = root.requireElement(query = "#bookimg > img", name = TAG_IMAGE) { it.absSrc() }
        val bookright = root.requireElement(query = "#bookinfo > div.bookright")
        val name = bookright.requireElement(query = "> div.booktitle > h1", name = TAG_NOVEL_NAME) { it.text() }
        val author = bookright.requireElement(query = "#author > a", name = TAG_AUTHOR_NAME) { it.text() }
        val info = bookright.getElements(query = "#bookintro > p") {
            it.joinToString("\n") {
                it.textNodes().joinToString("\n") {
                    it.toString().trim()
                }
            }
        }.toString()

        val update = bookright.getElement(query = "> div.new > span > span") {
            val updateString = it.text()
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            sdf.parse(updateString)
        } ?: Date(0)

        val chapterPageUrl = requester.url
        return NovelDetail(NovelItem(this, name, author, requester), img, update, info, chapterPageUrl)
    }

    override fun getNovelChaptersAsc(requester: Requester): List<NovelChapter> {
        val root = request(requester)
        return root.requireElements(query = "#readerlist > ul > li > a", name = TAG_CHAPTER_LINK).map { a ->
            NovelChapter(a.text(), a.absHref())
        }
    }

    override fun getNovelText(requester: Requester): NovelText {
        val root = request(requester)
        val content = root.requireElement(query = "#content", name = TAG_CONTENT)
        return NovelText(content.textList())
    }
}
