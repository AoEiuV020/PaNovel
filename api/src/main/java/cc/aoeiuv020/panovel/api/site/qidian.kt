package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.pick
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
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
            url = "/search?kw=$it"
        }
        /*
        <a href="//book.qidian.com/info/3548786" target="_blank" data-eid="qd_S05" data-bid="3548786" data-algrid="0.0.0">重生之<cite class="red-kw">都市</cite>修仙</a>
         */
        document {
            items("#result-list > div > ul > li") {
                name("> div.book-mid-info > h4 > a")
                author("> div.book-mid-info > p.author")
            }
        }
    }
    // 详情页域名和首页不一样，
    detailTemplate = "//book.qidian.com/info/%s"
    detail {
        document {
            val detail = root.requireElement("body > div.wrap > div.book-detail-wrap.center990")
            val information = element("> div.book-information.cf > div.book-info", parent = detail)
            novel {
                name("> h1 > em", parent = information)
                author("h1 > span", parent = information) {
                    it.text().removeSuffix(" 著")
                }
            }
            image("#bookImg > img", parent = detail)
            introduction("div.book-intro > p", parent = detail)
            // 从章节列表中解析更新时间，可能整个章节列表都不存在
            // 虽然小说详情版块也有更新时间，但格式模糊，可能是几小时前这样，
            update("#j-catalogWrap > div.volume-wrap > div:nth-last-child(1) > ul > li:nth-last-child(1) > a", format = "yyyy-MM-dd HH:mm:ss") {
                it.title().pick("首发时间：(.*) 章节字数：.*").first()
            }
        }
    }
    chapterTemplate = "$detailTemplate#Catalog"
    chapters {
        val bookId = findBookId(it)
        // 用到的接口需要cookies中的_csrfToken参数，
        // 如果没有，就额外拿一遍详情页，取其中返回的_csrfToken，
        // TODO: 缓存cookies的话，_csrfToken不知道会不会过期，有必要测试下如果过期会拿到什么，至少一两天不会过期，
        val token = cookies["_csrfToken"] ?: run {
            response(connect(getNovelDetailUrl(bookId))).cookie("_csrfToken")
        }
        get {
            url = "https://book.qidian.com/ajax/book/category?_csrfToken=$token&bookId=$bookId"
        }
        /*
{
  "data": {
    "isPublication": 0,
    "salesMode": 1,
    "vs": [
      {
        "vId": 70947019,
        "cCnt": 26,
        "vS": 0,
        "isD": 0,
        "vN": "斧头帮的崛起",
        "cs": [
          {
            "uuid": 1,
            "cN": "第一章 白手起家斧头帮",
            "uT": "2018-04-21 09:40:53",
            "cnt": 2109,
            "cU": "M_bVs41zjLaRTIpqx7GUJA2/uU0Ubg1w3eLM5j8_3RRvhw2",
            "id": 404501446,
            "sS": 1
          }
        ],
        "wC": 54704,
        "hS": false
      }
    ],
    "chapterTotalCnt": 73,
    "firstChapterJumpurl": "//read.qidian.com/chapter/M_bVs41zjLaRTIpqx7GUJA2/uU0Ubg1w3eLM5j8_3RRvhw2",
    "loginStatus": 0,
    "hasRead": 0
  },
  "code": 0,
  "msg": "suc"
}
         */
        response {
            gson.fromJson(it, JsonObject::class.java)
                    .getAsJsonObject("data")
                    .getAsJsonArray("vs").flatMap {
                        it.asJsonObject.getAsJsonArray("cs").map {
                            it.asJsonObject.let {
                                val chapterName = it.getAsJsonPrimitive("cN").asString
                                val chapterId = it.getAsJsonPrimitive("id").asInt.toString()
                                val updateTime = try {
                                    val uT = it.getAsJsonPrimitive("uT").asString
                                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
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
    contentTemplate = "//m.qidian.com/book/%s"
    content {
        val bookIdAndChapterId = findChapterId(it)
        val args = bookIdAndChapterId.split('/')
        val bookId = args[0]
        val chapterId = args[1]
        get {
            url = "https://m.qidian.com/majax/chapter/getChapterInfo?bookId=$bookId&chapterId=$chapterId"
        }
        // 不删除这个Cookie就拿不到页面，或者把同样的参数放一份在get参数里，不知道起点怎么想的，
        requireNotNull(connection).request().removeCookie("_csrfToken")
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
    private val gson: Gson = GsonBuilder().create()
}

