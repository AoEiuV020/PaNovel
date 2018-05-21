package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.pick
import cc.aoeiuv020.panovel.api.*
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.base.JsoupNovelContext
import org.jsoup.Connection
import org.jsoup.nodes.Document
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.regex.Pattern

/**
 *
 * Created by AoEiuV020 on 2018.03.14-01:35:36.
 */
class Syxs : DslJsoupNovelContext() {init {
    site {
        name = "31小说"
        baseUrl = "http://www.31xs.net"
        logo = "https://imgsa.baidu.com/forum/w%3D580/sign=e8432cdf06d162d985ee621421dfa950/47e082d8bc3eb135d9b1d5a0aa1ea8d3fc1f44a6.jpg"
    }
    search {
        get {
            url = "/search.php?keywords=${gbk(it)}"
        }
        document {
            /*
            <tr>
                <td class="odd">[<a href="/list/3/1.html">都市</a>] <a href="http://www.31xs.com/13/13704/" target="_blank"><font color="red">都市</font>之极品仙官</a></td>
                <td class="odd"><a href="http://www.31xs.com/13/13704/10358379.html" target="_blank">第1037章 开杀戒</a></td>
                <td class="even">八方风云</td>
                <td class="odd">2018-05-21 05:26:24</td>
            </tr>
             */
            items("#content > table > tbody > tr:not(:nth-child(1))") {
                name(" > td:nth-child(1) > a:nth-child(2)")
                author(" > td.even")
            }
        }
    }
    bookIdRegex = firstTwoIntPattern
    // http://www.31xs.net/13/13011/
    detailTemplate = "/%s/"
    detail {
        document {
            val div = root.requireElement("#info")
            novel {
                name("> h1", parent = div)
                author("> p:nth-child(2)", parent = div, block = pickString("作    者：(\\S*)"))
            }
            image("#fmimg > img")
            introduction("#intro") {
                it.ownTextList().joinToString("\n")
            }
            update("head > meta[property=og:novel:update_time]", format = "yyyy-MM-dd HH:mm:ss") {
                it.attr("content")
            }
        }
    }
    chapters {
        // 开头 9 章可能是网站上显示的最新章节，和列表最后重复，
        // 但也可能没有这重复的 9 章，
        // 接着两章是js脚本，拿到的地址会是空，
        val list = document {
            items("#list > dl > dd > a")
        }
        var index = 0
        // 以防万一，
        if (list.size == 1) return@chapters list
        // 倒序列表判断是否重复章节，
        val reversedList = list.asReversed()
        list.dropWhile {
            (it == reversedList[index]
                    || it.extra.isBlank()).also { ++index }
        }
    }
    chapterIdRegex = firstThreeIntPattern
    // http://www.31xs.net/13/13011/8981866.html
    contentTemplate = "/%s.html"
    content {
        document {
            items("#content > p")
        }
    }
}
}

// 这类已经不用了，搞定dsl就删除，
@Suppress("ClassName", "unused")
class _Syxs : JsoupNovelContext() {
    companion object {
        private val SEARCH_PAGE_URL = "http://zhannei.baidu.com/cse/search"
    }

    /**
     * 这网站页面没有编码，要自己指定，
     */
    override val charset: String? = "GBK"

    override val site = NovelSite(
            name = "31小说",
            baseUrl = "http://www.31xs.net",
            logo = "https://imgsa.baidu.com/forum/w%3D580/sign=e8432cdf06d162d985ee621421dfa950/47e082d8bc3eb135d9b1d5a0aa1ea8d3fc1f44a6.jpg"
    )

    override fun check(url: String): Boolean {
        return super.check(url)
                || URL(url).host.startsWith("www.31xs.")
                || (isSearchResult(url) && url.contains("7845455592055299828"))
    }

    private fun isSearchResult(url: String): Boolean {
        return url.startsWith(SEARCH_PAGE_URL)
    }

    override fun connectByNovelName(name: String): Connection {
        // TODO: 这网站已经有自己的搜索了，
        val key = URLEncoder.encode(name, "UTF-8")
        return connect("$SEARCH_PAGE_URL?s=7845455592055299828&q=$key")
    }

    override fun searchNovelName(name: String): List<NovelItem> {
        // 搜索是百度的，搜索结果页面是UTF-8,
        return getSearchResultList(parse(connectByNovelName(name), charset = "UTF-8"))
    }

    override fun getSearchResultList(root: Document): List<NovelItem> {
        return root.requireElements("#results > div.result-list > div > div.result-game-item-detail").map {
            val a = it.requireElement("h3 > a", TAG_NOVEL_LINK)
            val name = a.title()
            val bookId = findBookId(a.href())
            val author = it.requireElement("> div > p:nth-child(1) > span:nth-child(2)") { it.text().trim() }
            NovelItem(this, name, author, bookId)
        }
    }

    override val bookIdRegex: Pattern
        get() = firstTwoIntPattern

    // http://www.31xs.net/13/13011/
    override val detailTemplate: String?
        get() = "/%s/"

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelDetail(root: Document): NovelDetail {
        val img = root.requireElement("#fmimg > img", TAG_IMAGE) { it.absSrc() }
        val div = root.requireElement("#info")
        val name = div.requireElement("> h1", TAG_NOVEL_NAME) { it.text() }
        val (author) = div.requireElement("> p:nth-child(2)", TAG_AUTHOR_NAME) { it.text() }
                .pick("作    者：(\\S*)")
        val intro = root.requireElement("#intro") {
            it.ownTextList().joinToString("\n")
        }.toString()
        val update = root.getElement("head > meta[property=og:novel:update_time]") {
            val updateString = it.attr("content")
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            sdf.parse(updateString)
        }

        val bookId = findFirstOneInt(root.location())
        return NovelDetail(NovelItem(this, name, author, bookId), img, update, intro, bookId)
    }

    override fun getNovelChaptersAsc(root: Document): List<NovelChapter> {
        return root.requireElements("#list > dl > dd > a", TAG_CHAPTER_LINK).dropWhile { it.text() != "加入书架" }.drop(1).map { a ->
            NovelChapter(a.text(), a.path())
        }
    }

    override val chapterIdRegex: Pattern
        get() = firstThreeIntPattern

    // http://www.31xs.net/13/13011/8981866.html
    override val contentTemplate: String?
        get() = "/%s.html"

    override fun getNovelText(root: Document): NovelText {
        return NovelText(root.requireElements("#content > p", TAG_CONTENT).ownTextList())
    }
}
