@file:Suppress("UnnecessaryVariable")

package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.pick
import cc.aoeiuv020.panovel.api.*
import org.jsoup.Connection
import org.jsoup.nodes.Document
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

/**
 *
 * Created by AoEiuV020 on 2017.10.08-21:03:33.
 */
class Biquge : JsoupNovelContext() {
    override val site = NovelSite(
            name = "笔趣阁",
            baseUrl = "https://www.biqubao.com",
            logo = "https://imgsa.baidu.com/forum/w%3D580/sign=1d712d8332dbb6fd255be52e3925aba6/d7d2c843fbf2b211dfb81c36c18065380dd78e1b.jpg"
    )


    override fun connectByNovelName(name: String): Connection {
        val key = URLEncoder.encode(name, "UTF-8")
        return connect(absUrl("/search.php?keyword=$key"))
    }

    override fun getSearchResultList(root: Document): List<NovelItem> {
        return root.requireElements(name = TAG_SEARCH_RESULT_LIST, query = "div.result-list > div").map {
            val a = it.requireElement(name = TAG_NOVEL_LINK, query = "> div.result-game-item-detail > h3 > a")
            val name = a.title()
            val bookId = findBookId(a.href())
            val author = it.requireElement(name = TAG_AUTHOR_NAME, query = "> div.result-game-item-detail > div > p:nth-child(1) > span:nth-child(2)") {
                it.text().trim()
            }
            NovelItem(this, name, author, bookId)
        }
    }

    override fun check(url: String): Boolean {
        return super.check(url)
                || URL(url).host == "www.biquge.cn"
    }

    override val detailTemplate: String
        get() = "/book/%s/"

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelDetail(root: Document): NovelDetail {
        val img = root.requireElement(query = "#fmimg > img", name = TAG_IMAGE) { it.src() }
        val div = root.requireElement(query = "#info")
        val name = div.requireElement(query = "> h1", name = TAG_NOVEL_NAME) { it.text() }
        val (author) = div.requireElement(query = "> p:nth-child(2)", name = TAG_AUTHOR_NAME) {
            it.text().pick("作    者：(\\S*)")
        }
        val intro = root.getElements("#intro > p:not(:nth-last-child(1))") {
            it.ownTextList().joinToString("\n")
        }.toString()

        val update = div.getElement(query = "#info > p:nth-child(4)") {
            val (updateString) = it.text().pick("最后更新：(.*)")
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            sdf.parse(updateString)
        } ?: Date(0)

        val bookId = findBookId(root.location())
        return NovelDetail(NovelItem(this, name, author, bookId), img, update, intro, bookId)
    }

    override fun getNovelChaptersAsc(root: Document): List<NovelChapter> {
        /*
        <a href="/book/1196/443990.html">第一章 觉醒日</a>
         */
        return root.requireElements(query = "#list > dl > dd > a", name = TAG_CHAPTER_LINK).map { a ->
            NovelChapter(a.text(), a.path())
        }
    }

    override val contentTemplate: String
        get() = "/book/%s.html"

    override fun getNovelText(root: Document): NovelText {
        val content = root.requireElement(query = "#content", name = TAG_CONTENT)
        return NovelText(content.ownTextList())
    }
}
