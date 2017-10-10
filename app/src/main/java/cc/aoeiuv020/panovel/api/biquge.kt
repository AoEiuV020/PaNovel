@file:Suppress("UnnecessaryVariable")

package cc.aoeiuv020.panovel.api

import android.annotation.SuppressLint
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat

/**
 *
 * Created by AoEiuV020 on 2017.10.08-21:03:33.
 */
class Biquge : NovelContext() {
    companion object {
        private val SEARCH_PAGE_URL = "http://zhannei.baidu.com/cse/search"
    }

    private val site = NovelSite(
            name = "笔趣阁",
            baseUrl = "http://www.biqubao.com/",
            logo = "https://imgsa.baidu.com/forum/w%3D580/sign=1d712d8332dbb6fd255be52e3925aba6/d7d2c843fbf2b211dfb81c36c18065380dd78e1b.jpg"
    )

    override fun getNovelSite(): NovelSite = site

    override fun getGenres(): List<NovelGenre> {
        val root = request(site.baseUrl)
        val elements = root.select("#wrapper > div.nav > ul > li:not(:nth-last-child(1)):not(:nth-child(1)) > a")
        return elements.map {
            val a = it
            NovelGenre(a.text(), a.absHref())
        }
    }

    override fun getNextPage(genre: NovelGenre): NovelGenre? {
        if (!isSearchResult(genre.requester.url)) {
            return null
        }
        val root = request(genre.requester)
        val a = root.select("#pageFooter > a.pager-next-foot.n").first() ?: return null
        val url = a.absHref()
        if (url.isEmpty()) return null
        return NovelGenre(genre.name, url)
    }

    @SuppressLint("SimpleDateFormat")
    override fun getNovelList(requester: ListRequester): List<NovelListItem> {
        val root = request(requester)
        return if (isSearchResult(requester.url)) {
            root.select("#results > div.result-list > div > div.result-game-item-detail").map {
                val a = it.select("h3 > a").first()
                val name = a.title()
                val url = a.absHref()
                val author = it.select("> div > p:nth-child(1) > span:nth-child(2)").first().text().trim()
                val genre = it.select("> div > p:nth-child(2) > span:nth-child(2)").first().text()
                val last = it.select("> div > p:nth-child(4) > a").first().text().trim()
                val update = it.select("> div > p:nth-child(3) > span:nth-child(2)").first().text()
                val about = it.select(" > p").first().text()
                val info = "最新章节: $last 类型: $genre 更新: $update 简介: $about"
                NovelListItem(NovelItem(this, name, author, url), info)
            }
        } else if (requester.url.startsWith("http://www.biqubao.com/quanben/")) {
            root.select("#main > div.novelslist2 > ul > li:not(:nth-child(1))").map {
                val a = it.select("> span.s2 > a").first()
                val name = a.text()
                val url = a.absHref()
                val author = it.select("> span.s4").first().text()
                val last = it.select("> span.s3 > a").first().text()
                val update = it.select("> span.s5").first().text()
                val genre = it.select("> span.s1 > a").first().text()
                val info = "最新章节: $last 类型: $genre 更新: $update"
                NovelListItem(NovelItem(this, name, author, url), info)
            }
        } else {
            val list1 = root.select("#hotcontent > div > div").map {
                val a = it.select("> dl > dt > a").first()
                val name = a.text()
                val url = a.absHref()
                val author = it.select("> dl > dt > span").first().text()
                val info = it.select("> dl > dd").first().text().trim()
                NovelListItem(NovelItem(this, name, author, url), info)
            }
            val list2 = root.select("#newscontent > div.l > ul > li").map {
                val a = it.select("> span.s2 > a").first()
                val name = a.text()
                val url = a.absHref()
                val author = it.select("> span.s5").first().text()
                val info = it.select("> span.s3").first().text()
                NovelListItem(NovelItem(this, name, author, url), info)
            }
            val list3 = root.select("#newscontent > div.r > ul > li").map {
                val a = it.select("> span.s2 > a").first()
                val name = a.text()
                val url = a.absHref()
                val author = it.select("> span.s5").first().text()
                val info = ""
                NovelListItem(NovelItem(this, name, author, url), info)
            }
            list1 + list2 + list3
        }
    }

    override fun check(url: String): Boolean {
        return super.check(url)
                || (isSearchResult(url) && url.contains("11522483553330821378"))
                || URL(url).host == "www.biquge.cn"
    }

    /**
     * 这网站用的是百度站内搜索，
     * 连接时不时失败，应该是百度的问题，
     */
    override fun searchNovelName(name: String): NovelGenre {
        val key = URLEncoder.encode(name, "UTF-8")
        return NovelGenre(name, "$SEARCH_PAGE_URL?s=11522483553330821378&q=$key")
    }

    private fun isSearchResult(url: String): Boolean {
        return url.startsWith(SEARCH_PAGE_URL)
    }

    @SuppressLint("SimpleDateFormat")
    override fun getNovelDetail(requester: DetailRequester): NovelDetail {
        val root = request(requester)
        val img = root.select("#fmimg > img").first().src()
        val genre = root.select("div.con_top > a:nth-child(2)").first().text()
        val div = root.select("#info").first()
        val name = div.select("> h1").first().text()
        val (author) = div.select("> p:nth-child(2)").first().text()
                .pick("作    者：(\\S*)")
        val (status) = div.select("#info > p:nth-child(3)").first().text()
                .pick("状    态：([^,]*)")
        val (updateString) = div.select("#info > p:nth-child(4)").first().text()
                .pick("最后更新：(.*)")
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val update = sdf.parse(updateString)
        val lastChapter = div.select("> p:nth-child(5) > a").first().let {
            NovelChapter(it.text(), it.absHref())
        }
        val length = "null"
        val info = root.select("#intro > p").joinToString("\n") {
            it.textNodes().joinToString("\n") {
                it.toString().trim()
            }
        }
        val stars = -1

        val chapterPageUrl = requester.url
        return NovelDetail(NovelItem(this, name, author, requester), img, update, lastChapter, status, genre, length, info, stars, chapterPageUrl)
    }

    override fun getNovelChaptersAsc(requester: ChaptersRequester): List<NovelChapter> {
        val root = request(requester.url)
        return root.select("#list > dl > dd > a").map {
            val a = it
            NovelChapter(a.text(), a.absHref())
        }
    }

    override fun getNovelText(requester: TextRequester): NovelText {
        val root = request(requester)
        val content = root.select("#content").first()
        return NovelText(content.textList())
    }
}
