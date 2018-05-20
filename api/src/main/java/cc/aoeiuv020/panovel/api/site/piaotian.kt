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
import java.util.regex.Pattern

/**
 *
 * Created by AoEiuV020 on 2017.10.02-16:03:02.
 */
class Piaotian : JsoupNovelContext() {
    override val site = NovelSite(
            name = "飘天文学",
            baseUrl = "https://www.piaotian.com",
            logo = "https://www.piaotian.com/css/logo.gif"
    )

    private fun isDetail(url: String) = URL(url).path.startsWith("/bookinfo")

    override fun connectByNovelName(name: String): Connection {
        val key = URLEncoder.encode(name, "GBK")
        // 加上&page=1可以避开搜索时间间隔的限制，
        // 之前是通过不加载cookies避开搜索时间间隔的限制，
        return connect(realUrl("/modules/article/search.php?searchtype=articlename&searchkey=$key&page=1"))
    }

    @SuppressWarnings("SimpleDateFormat")
    override fun searchNovelName(name: String): List<NovelListItem> {
        val root = parse(connectByNovelName(name))
        // 搜索结果可能直接跳到详情页，
        return if (isDetail(root.location())) {
            val detail = getNovelDetail(root)
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val info = detail.run { "更新: ${sdf.format(update)} 简介: $introduction" }
            listOf(NovelListItem(detail.novel, info))
        } else {
            getSearchResultList(root)
        }
    }

    override fun getSearchResultList(root: Document): List<NovelListItem> {
        val elements = root.requireElements(query = "#content > table.grid > tbody > tr:not(:nth-child(1))")
        return elements.map {
            /*
            <a href="https://www.piaotian.com/bookinfo/9/9312.html">都市最强装逼系统</a>
             */
            val a = it.requireElement(query = "td:nth-child(1) > a", name = TAG_NOVEL_LINK)
            val name = a.text()
            val bookId = findBookId(a.href())
            val author = it.requireElement(query = "td:nth-child(3)", name = TAG_AUTHOR_NAME) { it.text() }
            val length = it.getElement(query = "td:nth-child(4)") { it.text() }
            val last = it.getElement(query = "td:nth-child(2) > a") { it.text() }
            val update = it.getElement(query = "td:nth-child(5)") { it.text() }
            val status = it.getElement(query = "td:nth-child(6)") { it.text() }
            val info = "最新章节: $last 字数: $length 更新: $update 状态: $status"
            NovelListItem(NovelItem(this, name, author, bookId), info)
        }
    }

    override fun getNextPage(root: Document): String? {
        return root.getElement("#pagelink > a.next") { a ->
            a.absHref()
        }
    }

    override val bookIdRegex: Pattern
        get() = firstTwoIntPattern

    override val detailTemplate: String
        get() = "/bookinfo/%s.html"

    override fun getNovelDetail(root: Document): NovelDetail {
        val tbody1 = root.requireElement(query = "#content > table > tbody")
        val tbody2 = tbody1.requireElement(query = "tr:nth-child(1) > td > table > tbody")
        val pattern = "" +
                "(\\S*)\\s" +
                "类    别：(\\S*)\\s" +
                "作    者：(\\S*)\\s" +
                "管 理 员：(\\S*)\\s" +
                "全文长度：(\\S*)\\s" +
                "最后更新：(\\S*)\\s" +
                "文章状态：(\\S*)\\s" +
                "授权级别：(\\S*)\\s" +
                "首发状态：(\\S*)\\s" +
                "收 藏 数：(\\S*)\\s" +
                "总推荐数：(\\S*)\\s" +
                "本月推荐：(\\S*)\\s" +
                "收到鲜花：(\\S*)" +
                ""
        val list = tbody2.text().pick(pattern)
        val (name, _, author) = list

        val td = tbody1.requireElement(query = "tr:nth-child(4) > td > table > tbody > tr > td:nth-child(2)")
        val img = td.requireElement(query = "a > img", name = TAG_IMAGE) { it.src() }
        val intro = td.requireElement(query = "div") {
            it.textList().joinToString("\n")
        }

        val update = try {
            val (updateString) = list.drop(5)
            val lastChapterElement = tbody1.requireElement("tr:nth-child(8) > td > table > tbody > tr:nth-child(1) > td:nth-child(1) > li > a")
            val (year) = updateString.pick("(\\d*)-(\\d*)-(\\d*)")
            val (month, day, hour, minute) = lastChapterElement.title().pick(".*更新时间:(\\d*)-(\\d*) (\\d*):(\\d*)")
            @Suppress("DEPRECATION")
            Date(year.toInt() - 1900, month.toInt() - 1, day.toInt(), hour.toInt(), minute.toInt())
        } catch (e: Exception) {
            logger.error("更新时间解析失败", e)
            Date(0)
        }

        /*
        <a href="https://www.piaotian.com/html/8/8955/index.html">(查看全部章节)</a>
         */
        val chapterPageUrl = tbody1.requireElement("tr:nth-child(8) > td > table > caption > a") { it.path() }
        return NovelDetail(NovelItem(this, name, author, URL(root.location()).path), img, update, intro, chapterPageUrl)
    }

    override val chapterTemplate: String
        get() = "/html/%s/index.html"

    override fun getNovelChaptersAsc(root: Document): List<NovelChapter> {
        return root.requireElements("div.mainbody > div.centent > ul > li > a", name = TAG_CHAPTER_LINK) {
            it.dropWhile {
                it.absHref().isEmpty() || it.href() == "#"
            }.dropLastWhile {
                it.text().isEmpty()
            }.map { a ->
                /*
                <a href="5858438.html">第十九章 开脉</a>
                https://www.piaotian.com/html/9/9559/6425277.html
                 */
                NovelChapter(a.text(), a.path())
            }
        }
    }

    override val chapterIdRegex: Pattern
        get() = firstThreeIntPattern

    override val contentTemplate: String
        get() = "/html/%s.html"

    override fun getNovelText(root: Document): NovelText {
        return NovelText(root.body().textList())
    }
}