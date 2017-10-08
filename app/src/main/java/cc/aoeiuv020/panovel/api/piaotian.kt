@file:Suppress("UnnecessaryVariable")

package cc.aoeiuv020.panovel.api

import android.annotation.SuppressLint
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

/**
 *
 * Created by AoEiuV020 on 2017.10.02-16:03:02.
 */
class Piaotian : NovelContext() {
    companion object {
        private val SEARCH_PAGE_URL = "http://www.piaotian.com/modules/article/search.php"
    }

    private val site = NovelSite(
            name = "飘天文学",
            baseUrl = "http://www.piaotian.com/",
            logo = "http://www.piaotian.com/css/logo.gif"
    )

    override fun getNovelSite(): NovelSite = site

    override fun getGenres(): List<NovelGenre> {
        val root = request(site.baseUrl)
        val elements = root.select("div.navinner > ul > li:not(:nth-last-child(1)):not(:nth-child(1)) > a")
        return elements.map {
            val a = it
            NovelGenre(a.text(), a.absHref())
        }
    }

    override fun getNextPage(genre: NovelGenre): NovelGenre? {
        val root = request(genre.requester)
        val a = root.select("#pagelink > a.next").first() ?: return null
        val url = a.absHref()
        return NovelGenre(genre.name, url)
    }

    private fun isDetail(url: String) = url.startsWith("http://www.piaotian.com/bookinfo")

    @SuppressLint("SimpleDateFormat")
    override fun getNovelList(requester: ListRequester): List<NovelListItem> {
        val response = response(requester)
        if (isDetail(response.url().toString())) {
            val detail = getNovelDetail(DetailRequester(requester.url))
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val info = detail.run { "最新章节: ${lastChapter.name} 字数: $length 更新: ${sdf.format(update)} 状态: $status" }
            return listOf(NovelListItem(detail.novel, info))
        }
        val root = request(response)
        val elements = root.select("#content > table.grid > tbody > tr:not(:nth-child(1))")
        return elements.map {
            val a = it.select("td:nth-child(1) > a").first()
            val name = a.text()
            val url = a.absHref()
            val author = it.select("td:nth-child(3)").first().text()
            val length = it.select(("td:nth-child(4)")).first().text()
            val last = it.select("td:nth-child(2) > a").first().text()
            val update = it.select("td:nth-child(5)").first().text()
            val status = it.select("td:nth-child(6)").first().text()
            val info = "最新章节: $last 字数: $length 更新: $update 状态: $status"
            NovelListItem(NovelItem(this, name, author, url), info)
        }
    }

    private fun search(str: String, type: String): NovelGenre {
        val key = URLEncoder.encode(str, "GBK")
        return NovelGenre(str, "$SEARCH_PAGE_URL?searchtype=$type&searchkey=$key")
    }

    override fun searchNovelName(name: String) = search(name, "articlename")

    override fun searchNovelAuthor(author: String) = search(author, "author")

    fun isSearchResult(requester: ListRequester): Boolean {
        return requester.url.startsWith(SEARCH_PAGE_URL)
    }

    override fun getNovelDetail(requester: DetailRequester): NovelDetail {
        val root = request(requester)
        val tbody1 = root.select("#content > table > tbody").first()
        val tbody2 = tbody1.select("tr:nth-child(1) > td > table > tbody").first()
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
        val (name, genre, author, _, length) = list
        val (updateString, status, _, _, starsString) = list.drop(5)
        val stars = starsString.toInt()

        val td = tbody1.select("tr:nth-child(4) > td > table > tbody > tr > td:nth-child(2)").first()
        val img = td.select("a > img").first().src()
        val info = td.select("div").first().textList()
                .joinToString("\n")

        val lastChapterElement = tbody1.select("tr:nth-child(8) > td > table > tbody > tr:nth-child(1) > td:nth-child(1) > li > a").first()
        val (year) = updateString.pick("(\\d*)-(\\d*)-(\\d*)")
        val (month, day, hour, minute) = lastChapterElement.title().pick(".*更新时间:(\\d*)-(\\d*) (\\d*):(\\d*)")
        @Suppress("DEPRECATION")
        val update = Date(year.toInt() - 1900, month.toInt() - 1, day.toInt(), hour.toInt(), minute.toInt())
        val lastChapter = lastChapterElement.let {
            val a = it
            NovelChapter(a.text(), a.absHref())
        }
        val chapterPageUrl = tbody1.select("tr:nth-child(8) > td > table > caption > a").first().absHref()
        return NovelDetail(NovelItem(this, name, author, requester), img, update, lastChapter, status, genre, length, info, stars, chapterPageUrl)
    }

    override fun getNovelChaptersAsc(requester: ChaptersRequester): List<NovelChapter> {
        val root = request(requester.url)
        return root.select("div.mainbody > div.centent > ul > li > a").dropWhile {
            it.absHref().isEmpty() || it.href() == "#"
        }.dropLastWhile {
            it.text().isEmpty()
        }.map {
            val a = it
            NovelChapter(a.text(), a.absHref())
        }
    }

    override fun getNovelText(requester: TextRequester): NovelText {
        val root = request(requester)
        return NovelText(root.body().textList())
    }
}