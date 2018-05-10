package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.pick
import cc.aoeiuv020.panovel.api.*
import java.net.URLEncoder
import java.text.SimpleDateFormat

/**
 * Created by AoEiuV020 on 2018.05.10-18:11:57.
 */
class Qlyx : JsoupNovelContext() {
    override val site: NovelSite = NovelSite(
            name = "齐鲁文学",
            baseUrl = "http://www.76wx.com/",
            logo = "http://www.76wx.com/images/book_logo.png"
    )

    private fun search(str: String, type: String): NovelGenre {
        val key = URLEncoder.encode(str, "GBK")
        // 突然发现，加上&page=1就没有搜索时间间隔的限制了，无所谓，删除cookie也一样，
        val url = "http://www.76wx.com/modules/article/search.php?searchtype=$type&searchkey=$key"
        return NovelSearch(str, url)
    }

    override fun searchNovelName(name: String) = search(name, "articlename")

    override fun searchNovelAuthor(author: String) = search(author, "author")

    override fun getNextPage(genre: NovelGenre): NovelGenre? {
        val root = request(genre.requester)
        return root.getElement("#pagelink > a.next") {
            NovelSearch(genre.name, it.absHref())
        }
    }

    private fun isDetail(url: String) = url.startsWith("http://www.76wx.com/book")

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelList(requester: ListRequester): List<NovelListItem> {
        val response = if (requester is SearchListRequester) {
            // 搜索页不装载cookie, 避开搜索时间间隔的限制，
            connect(requester).execute()
        } else {
            response(requester)
        }
        if (isDetail(response.url().toString())) {
            val detail = getNovelDetail(DetailRequester(response.url().toString()))
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val info = detail.run { "更新: ${sdf.format(update)} 简介: $introduction" }
            return listOf(NovelListItem(detail.novel, info))
        }
        val root = request(response)
        val elements = root.requireElements(name = "搜索结果列表", query = "#main > table > tbody > tr:not(:nth-child(1))")
        return elements.map {
            val (name, url) = it.requireElement(query = "td:nth-child(1) > a") {
                it.text() to it.absHref()
            }
            val author = it.requireElement(query = "td:nth-child(3)", name = TAG_AUTHOR_NAME) {
                it.text()
            }
            val length = it.getElement(query = "td:nth-child(4)") {
                it.text()
            }
            val last = it.getElement(query = "td:nth-child(2) > a") {
                it.text()
            }
            val update = it.getElement(query = "td:nth-child(5)") {
                it.text()
            }
            val status = it.getElement(query = "td:nth-child(6)") {
                it.text()
            }
            val intro = "最新章节: $last 字数: $length 更新: $update 状态: $status"
            NovelListItem(NovelItem(this, name, author, url), intro)
        }
    }

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelDetail(requester: DetailRequester): NovelDetail {
        val root = request(requester)
        val eMaininfo = root.requireElement(query = "#maininfo")
        val eInfo = eMaininfo.requireElement(query = "#info")
        val img = root.requireElement(query = "#fmimg > img") {
            it.absSrc()
        }
        val name = eInfo.requireElement(query = "> h1") {
            it.text()
        }
        val (author) = eInfo.requireElement(query = "> p:nth-child(2)", name = TAG_AUTHOR_NAME) {
            it.text().pick("作    者：(\\S*)")
        }
        val info = root.requireElement(query = "#intro > p", name = TAG_INTRODUCTION) {
            it.text().replaceWhiteWithNewLine()
        }

        val update = eInfo.requireElement(query = "> p:nth-child(4)", name = TAG_UPDATE_TIME) {
            val (updateString) = it.text().pick("更新时间：(.*)")
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            sdf.parse(updateString)
        }

        val chapterPageUrl = requester.url

        return NovelDetail(NovelItem(this, name, author, requester), img, update, info, chapterPageUrl)
    }

    override fun getNovelChaptersAsc(requester: ChaptersRequester): List<NovelChapter> {
        val root = request(requester)
        return root.requireElement(query = "#list > dl") {
            it.children()
                    // 删除第一个dt,
                    .drop(1)
                    // 删除第二个dt前的dd,
                    .dropWhile {
                        it.tagName() != "dt"
                    }
                    // 删除第二个dt,
                    .drop(1)
        }.map { dd ->
            val a = dd.requireElement(query = "> a", name = TAG_CHAPTER_LINK)
            NovelChapter(a.text(), a.absHref())
        }
    }

    override fun getNovelText(requester: TextRequester): NovelText {
        val root = request(requester)
        val content = root.requireElement(query = "#content", name = TAG_CONTENT)
        return NovelText(content.textList())
    }
}