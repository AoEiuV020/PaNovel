package cc.aoeiuv020.panovel.api

import android.annotation.SuppressLint
import java.net.URLEncoder
import java.text.SimpleDateFormat

/**
 *
 * Created by AoEiuV020 on 2017.10.11-19:39:27.
 */
class Liudatxt : NovelContext() {
    companion object {
        private val SEARCH_PAGE_URL = "http://www.liudatxt.com/search.php"
    }

    private val site = NovelSite(
            name = "溜达小说",
            baseUrl = "http://www.liudatxt.com/",
            logo = "https://imgsa.baidu.com/forum/w%3D580/sign=1b4c19b5f0edab6474724dc8c737af81/4afa9ae93901213f074d29a25fe736d12e2e95b9.jpg"
    )

    override fun getNovelSite(): NovelSite = site

    override fun getGenres(): List<NovelGenre> {
        val root = request(site.baseUrl)
        val elements = root.select("#header > div.nav > ul > li > a").drop(1).dropLast(1)
        return elements.map { a ->
            NovelGenre(a.text(), a.absHref())
        }
    }

    override fun getNextPage(genre: NovelGenre): NovelGenre? {
        if (isSearchResult(genre.requester.url)) {
            return null
        }
        val root = request(genre.requester)
        val a = root.select("#main > div.list_center > div.pages > a:contains(下一页)").first() ?: return null
        val url = a.absHref()
        if (url.isEmpty()) return null
        return NovelGenre(genre.name, url)
    }

    @SuppressLint("SimpleDateFormat")
    override fun getNovelList(requester: ListRequester): List<NovelListItem> {
        val root = request(requester)
        val flag = isSearchResult(requester.url)
        return root.select("#${if (flag) "sitembox" else "sitebox"} > dl").map {
            val a = it.select("> dd:nth-child(2) > h3 > a").first()
            val name = a.text()
            val url = a.absHref()
            val dd3 = it.select("> dd:nth-child(3)").first()
            val author = dd3.select("> span:nth-child(1)").first().text()
            val status = dd3.select("> span:nth-child(2)").first().text()
            val genre = dd3.select("> span:nth-child(3)").first().text()
            val last = it.select("> dd:nth-child(5) > a").first().text().trim()
            val about = it.select("> dd.book_des").first().text()
            val info = if (flag) {
                val length = dd3.select("> span:nth-child(4)").first().text()
                val update = it.select("> dd:nth-child(5) > span").first().text()
                "最新章节: $last 类型: $genre 更新: $update 状态: $status 长度: $length 简介: $about"
            } else {
                val update = it.select("> dd:nth-child(2) > h3 > span").first().text()
                "最新章节: $last 类型: $genre 更新: $update 状态: $status 简介: $about"
            }
            NovelListItem(NovelItem(this, name, author, url), info)
        }
    }

    /**
     * 这网站用的是百度站内搜索，
     * 连接时不时失败，应该是百度的问题，
     */
    override fun searchNovelName(name: String): NovelGenre {
        val key = URLEncoder.encode(name, "UTF-8")
        return NovelGenre(name, "$SEARCH_PAGE_URL?searchkey=$key")
    }

    private fun isSearchResult(url: String): Boolean {
        return url.startsWith(SEARCH_PAGE_URL)
    }

    @SuppressLint("SimpleDateFormat")
    override fun getNovelDetail(requester: DetailRequester): NovelDetail {
        val chapterRoot = request(requester)
        val detail = chapterRoot.select("#main > div.coverecom > div.tabstit > table > tbody > tr > td:nth-child(1) > a:nth-child(4)").first()
        val root = request(detail.absHref())
        val img = root.select("#bookimg > img").first().absSrc()
        val bookright = root.select("#bookinfo > div.bookright").first()
        val name = bookright.select("> div.booktitle > h1").first().text()
        val author = bookright.select("#author > a").first().text()
        val list = bookright.select("> div.count > ul > li > span").map { it.text() }
        val (genre, weekClick, _, _, status) = list
        val (_, length) = list.drop(5)
        val updateString = bookright.select("> div.new > span > span").first().text()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val update = sdf.parse(updateString)
        val stars = weekClick.toInt()
        val info = bookright.select("#bookintro > p").joinToString("\n") {
            it.textNodes().joinToString("\n") {
                it.toString().trim()
            }
        }
        val lastChapter = root.select("#newlist > ul > li:nth-child(1) > a").first().let {
            NovelChapter(it.text(), it.absHref())
        }

        val chapterPageUrl = requester.url
        return NovelDetail(NovelItem(this, name, author, requester), img, update, lastChapter, status, genre, length, info, stars, chapterPageUrl)
    }

    override fun getNovelChaptersAsc(requester: ChaptersRequester): List<NovelChapter> {
        val root = request(requester.url)
        return root.select("#readerlist > ul > li > a").map { a ->
            NovelChapter(a.text(), a.absHref())
        }
    }

    override fun getNovelText(requester: TextRequester): NovelText {
        val root = request(requester)
        val content = root.select("#content").first()
        return NovelText(content.textList())
    }
}
