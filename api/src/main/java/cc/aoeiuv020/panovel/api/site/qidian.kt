package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.anull.notNull
import cc.aoeiuv020.base.jar.absHref
import cc.aoeiuv020.base.jar.splitNewLine
import cc.aoeiuv020.base.jar.textListSplitWhitespace
import cc.aoeiuv020.base.jar.title
import cc.aoeiuv020.gson.GsonUtils
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.regex.pick
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import okhttp3.Cookie
import okhttp3.HttpUrl
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.*

/**
 *
 * Created by AoEiuV020 on 2017.10.16-17:40:38.
 */
class Qidian : DslJsoupNovelContext() {init {
    site {
        name = "起点中文"
        baseUrl = "https://www.qidian.com"
        logo = "https://qidian.gtimg.com/qd/images/logo.dbed5.png"
    }
    search {
        get {
            url = "/search"
            data {
                "kw" to it
            }
        }
        /*
        <a href="//book.qidian.com/info/3548786" target="_blank" data-eid="qd_S05" data-bid="3548786" data-algrid="0.0.0">重生之<cite class="red-kw">都市</cite>修仙</a>
         */
        document {
            items("#result-list > div > ul > li") {
                name("> div.book-mid-info > h2 > a")
                author("> div.book-mid-info > p.author") {
                    // 有的小作者的名字不可点击，没有a标签，只能这样child获取，
                    it.child(1).text()
                }
            }
        }
    }
    // 详情页域名和首页不一样，
    detailPageTemplate = "//m.qidian.com/book/%s.html"
    detail {
        document {
            val detail = root.requireElement("body > div.page.page-book-detail")
            val information = element("#bookDetailWrapper > div > div > div", parent = detail)
            novel {
                name("> h2", parent = information)
                author("> div.book-rand-a > a", parent = information) {
                    it.ownText()
                }
            }
            image("#bookDetailWrapper > div > div > img", parent = detail)
            introduction("#bookSummary > textarea", parent = detail) {
                it.textListSplitWhitespace().joinToString("\n") { it.removeSuffix("<br>") }
            }
        }
    }
    chaptersPageTemplate = "https://m.qidian.com/book/%s/catalog/"
    chapters {
        document {
            val bookId = findBookId(it)
            val line = root.html().splitNewLine().filter { it.startsWith("g_data.volumes = ") }.single()
            val json = line.removePrefix("g_data.volumes = ").removeSuffix(";")
            novelChapterList = gson.fromJson(json, JsonArray::class.java).flatMap {
                it.asJsonObject.getAsJsonArray("cs").map {
                    it.asJsonObject.let {
                        val chapterName = it.getAsJsonPrimitive("cN").asString
                        val chapterId = it.getAsJsonPrimitive("id").asInt.toString()
                        val updateTime = try {
                            val uT = it.getAsJsonPrimitive("uT").asString
                            val sdf = SimpleDateFormat("yyyy-MM-dd  HH:mm", Locale.CHINA)
                            sdf.parse(uT)
                        } catch (e: Exception) {
                            null
                        }
                        NovelChapter(chapterName, "$bookId/$chapterId", updateTime)
                    }
                }
            }
        }
    }
    // https://m.qidian.com/book/1010136878/381295976
    contentPageTemplate = "//m.qidian.com/book/%s"
    content {
        val bookIdAndChapterId = findBookIdWithChapterId(it)
        val args = bookIdAndChapterId.split('/')
        val bookId = args[0]
        val chapterId = args[1]
        get {
            url = "https://m.qidian.com/majax/chapter/getChapterInfo"
            data {
                "bookId" to bookId
                "chapterId" to chapterId
            }
        }
        response {
            val json = gson.fromJson(it, JsonObject::class.java)
            val content = json.getAsJsonObject("data")
                    .getAsJsonObject("chapterInfo")
                    .getAsJsonPrimitive("content")
                    .asString
            document(Jsoup.parse(content)) {
                items("p")
            }
        }
    }
}

    // 用来解析章节api和正文api,
    private val gson: Gson = GsonUtils.gson

    override fun cookieFilter(url: HttpUrl, cookies: MutableList<Cookie>): MutableList<Cookie> {
        if (url.encodedPath() == "/majax/chapter/getChapterInfo") {
            // 不删除这个Cookie就拿不到页面，或者把同样的参数放一份在get参数里，不知道起点怎么想的，
            cookies.removeAll { it.name() == "_csrfToken" }
        }
        return cookies
    }
}

