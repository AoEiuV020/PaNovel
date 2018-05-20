package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.*
import org.jsoup.Connection
import org.jsoup.nodes.Document
import java.text.SimpleDateFormat
import java.util.*

/**
 *
 * Created by AoEiuV020 on 2017.10.11-19:39:27.
 */
class Liudatxt : JsoupNovelContext() {
    override val site = NovelSite(
            name = "溜达小说",
            baseUrl = "http://www.liudatxt.com",
            logo = "https://imgsa.baidu.com/forum/w%3D580/sign=1b4c19b5f0edab6474724dc8c737af81/4afa9ae93901213f074d29a25fe736d12e2e95b9.jpg"
    )

    override fun getNextPage(root: Document): String? {
        return root.getElement(query = "#main > div.list_center > div.pages > a:contains(下一页)") {
            val url = it.path()
            return url.takeIf { url.isNotEmpty() }
        }
    }

    override fun connectByNovelName(name: String): Connection {
        return connect(realUrl("/search.php")).data("searchkey", name).method(Connection.Method.POST)
    }

    override fun getSearchResultList(root: Document): List<NovelItem> {
        return root.requireElements(query = "#sitembox > dl").map {
            val a = it.requireElement(query = "> dd:nth-child(2) > h3 > a", name = TAG_NOVEL_LINK)
            val name = a.text()
            val bookId = findBookId(a.href())
            val dd3 = it.requireElement(query = "> dd:nth-child(3)")
            val author = dd3.requireElement(query = "> span:nth-child(1)", name = TAG_AUTHOR_NAME) { it.text() }
            NovelItem(this, name, author, bookId)
        }
    }

    override val detailTemplate: String
        get() = "/%s/"

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelDetail(root: Document): NovelDetail {
        val img = root.requireElement(query = "#bookimg > img", name = TAG_IMAGE) { it.absSrc() }
        val bookright = root.requireElement(query = "#bookinfo > div.bookright")
        val name = bookright.requireElement(query = "> div.booktitle > h1", name = TAG_NOVEL_NAME) { it.text() }
        val author = bookright.requireElement(query = "#author > a", name = TAG_AUTHOR_NAME) { it.text() }
        val intro = bookright.getElements(query = "#bookintro > p") {
            it.ownTextList().joinToString("\n")
        }.toString()

        val update = bookright.getElement(query = "> div.new > span > span") {
            val updateString = it.text()
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            sdf.parse(updateString)
        } ?: Date(0)

        val bookId = findBookId(root.location())
        return NovelDetail(NovelItem(this, name, author, bookId), img, update, intro, bookId)
    }

    override val chapterTemplate: String
        get() = "/so/%s/"

    override fun getNovelChaptersAsc(root: Document): List<NovelChapter> {
        return root.requireElements(query = "#readerlist > ul > li > a", name = TAG_CHAPTER_LINK).map { a ->
            /*
            <a href="/so/5861/9533241.html" title="第六章 二十岁前不入仙门，终生无望" target="_blank">第六章 二十岁前不入仙门，终生无望</a>
             */
            NovelChapter(a.text(), a.path())
        }
    }

    override val contentTemplate: String
        get() = "/so/%s.html"

    override fun getNovelText(root: Document): NovelText {
        val content = root.requireElement(query = "#content", name = TAG_CONTENT)
        // 去广告，"#content > i"都是广告，
        return NovelText(content.ownTextList())
    }
}
