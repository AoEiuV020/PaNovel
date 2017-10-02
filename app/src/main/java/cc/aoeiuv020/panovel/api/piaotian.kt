@file:Suppress("UnnecessaryVariable")

package cc.aoeiuv020.panovel.api

import android.annotation.SuppressLint
import java.text.SimpleDateFormat

/**
 *
 * Created by AoEiuV020 on 2017.10.02-16:03:02.
 */
class Piaotian : NovelContext() {
    private val site = NovelSite(
            name = "飘天文学",
            baseUrl = "http://www.piaotian.com/",
            logo = "http://www.piaotian.com/css/logo.gif",
            charset = "GBK"
    )

    override fun getNovelSite(): NovelSite = site

    override fun getGenres(): List<NovelGenre> {
        val root = get(site.baseUrl)
        val elements = root.select("div.navinner > ul > li:not(:nth-last-child(1)):not(:nth-child(1)) > a")
        return elements.map {
            val a = it
            NovelGenre(a.text(), a.absHref())
        }
    }

    override fun getNextPage(genre: NovelGenre): NovelGenre? {
        val root = get(genre.url)
        val a = root.select("#pagelink > a.next").first() ?: return null
        val url = a.absHref()
        return NovelGenre(genre.name, url)
    }

    override fun getNovelList(genre: NovelGenre): List<NovelListItem> {
        val root = post(genre.url, genre.parameters)
        val elements = root.select("#content > table.grid > tbody > tr:not(:nth-child(1))")
        return elements.map {
            val a = it.select("td:nth-child(1) > a").first()
            val name = a.text()
            val url = a.absHref()
            val author = it.select("td:nth-child(3)").first().text()
            val number = it.select(("td:nth-child(4)")).first().text()
            val last = it.select("td:nth-child(2) > a").first().text()
            val update = it.select("td:nth-child(5)").first().text()
            val status = it.select("td:nth-child(6)").first().text()
            val info = "最新章节: $last 字数: $number 更新: $update 状态: $status"
            NovelListItem(NovelItem(name, author), url, info)
        }
    }

    private fun search(str: String, type: String): NovelGenre {
        val url = "http://www.piaotian.com/modules/article/search.php"
        val key = str
        return NovelGenre(str, url, mapOf("searchtype" to type, "searchkey" to key))
    }

    override fun searchNovelName(name: String) = search(name, "articlename")

    override fun searchNovelAuthor(author: String) = search(author, "author")

    override fun isSearchResult(genre: NovelGenre): Boolean {
        return genre.url.matches(Regex("http://www.piaotian.com/modules/article/search.php"))
    }

    @SuppressLint("SimpleDateFormat")
    override fun getNovelDetail(novelDetailUrl: NovelDetailUrl): NovelDetail {
        val root = get(novelDetailUrl.url)
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
        val list = tbody2.text().pick(pattern)!!
        val (name, genre, author, _, length) = list
        val (updateString, status, _, _, starsString) = list.drop(5)
        val update = SimpleDateFormat("yyyy-MM-dd").parse(updateString)
        val stars = starsString.toInt()

        val td = tbody1.select("tr:nth-child(4) > td > table > tbody > tr > td:nth-child(2)").first()
        val img = td.select("a > img").first().src()
        val info = td.select("div").first().textList()
                .joinToString("\n")

        val chapterPageUrl = tbody1.select("tr:nth-child(8) > td > table > caption > a").first().absHref()
        val chapterRoot = get(chapterPageUrl)
        val chapterList = chapterRoot.select("div.mainbody > div.centent > ul > li > a").map {
            val a = it
            NovelChapter(a.text(), a.absHref())
        }
        return NovelDetail(NovelItem(name, author), img, update, status, genre, length, info, stars, chapterList)
    }

    override fun getNovelText(novelChapter: NovelChapter): NovelText {
        val root = get(novelChapter.url)
        return NovelText(root.body().textList())
    }
}