@file:Suppress("UnnecessaryVariable")

package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.pick
import cc.aoeiuv020.panovel.api.*
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat

/**
 *
 * Created by AoEiuV020 on 2017.10.08-21:03:33.
 */
class Biquge : JsoupNovelContext() {
    companion object {
        private val SEARCH_PAGE_URL = "http://zhannei.baidu.com/cse/search"
    }

    override val site = NovelSite(
            name = "笔趣阁",
            baseUrl = "https://www.biqubao.com/",
            logo = "https://imgsa.baidu.com/forum/w%3D580/sign=1d712d8332dbb6fd255be52e3925aba6/d7d2c843fbf2b211dfb81c36c18065380dd78e1b.jpg"
    )

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelList(requester: Requester): List<NovelListItem> {
        val root = request(requester)
        return root.requireElements(name = TAG_SEARCH_RESULT_LIST, query = "div.result-list > div").map {
            val a = it.requireElement(name = TAG_NOVEL_LINK, query = "> div.result-game-item-detail > h3 > a")
            val name = a.title()
            // TODO: 详情页地址要尽量不变，考虑到网站域名可能变，不用绝对地址，
            val url = a.absHref()
            val author = it.requireElement(name = TAG_AUTHOR_NAME, query = "> div.result-game-item-detail > div > p:nth-child(1) > span:nth-child(2)") {
                it.text().trim()
            }
            val genre = it.getElement(query = "> div.result-game-item-detail > div > p:nth-child(2) > span:nth-child(2)") {
                it.text().trim()
            }
            val last = it.getElement(query = "> div.result-game-item-detail > div > p:nth-child(4) > a") {
                it.text().trim()
            }
            val update = it.getElement(query = "> div.result-game-item-detail > div > p:nth-child(3) > span:nth-child(2)") {
                it.text().trim()
            }
            val intro = it.getElement(query = "> div.result-game-item-detail > p") {
                it.text().trim()
            }
            val info = "最新章节: $last 类型: $genre 更新: $update 简介: $intro"
            NovelListItem(NovelItem(this, name, author, url), info)
        }
    }

    override fun check(url: String): Boolean {
        return super.check(url)
                || (isSearchResult(url) && url.contains("11522483553330821378"))
                || URL(url).host == "www.biquge.cn"
    }

    override fun getNovelItem(url: String): NovelItem {
        val bookId = findBookId(url)
        val detailUrl = "${site.baseUrl}book/$bookId/"
        return super.getNovelItem(detailUrl)
    }

    override fun searchNovelName(name: String): NovelGenre {
        val key = URLEncoder.encode(name, "UTF-8")
        val url = "${site.baseUrl}search.php?keyword=$key"
        return NovelGenre(name, url)
    }

    private fun isSearchResult(url: String): Boolean {
        return url.startsWith(SEARCH_PAGE_URL)
    }

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelDetail(requester: Requester): NovelDetail {
        val root = request(requester)
        val img = root.select("#fmimg > img").first().src()
        val div = root.select("#info").first()
        val name = div.select("> h1").first().text()
        val (author) = div.select("> p:nth-child(2)").first().text()
                .pick("作    者：(\\S*)")
        val info = root.select("#intro > p").joinToString("\n") {
            it.textNodes().joinToString("\n") {
                it.toString().trim()
            }
        }

        val (updateString) = div.select("#info > p:nth-child(4)").first().text()
                .pick("最后更新：(.*)")
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val update = sdf.parse(updateString)

        val chapterPageUrl = requester.url
        return NovelDetail(NovelItem(this, name, author, requester), img, update, info, chapterPageUrl)
    }

    override fun getNovelChaptersAsc(requester: Requester): List<NovelChapter> {
        val root = request(requester)
        return root.select("#list > dl > dd > a").map {
            val a = it
            NovelChapter(a.text(), a.absHref())
        }
    }

    override fun getNovelText(requester: Requester): NovelText {
        val root = request(requester)
        val content = root.select("#content").first()
        return NovelText(content.textList())
    }
}
