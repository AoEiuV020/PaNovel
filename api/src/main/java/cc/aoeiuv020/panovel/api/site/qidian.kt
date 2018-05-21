package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.debug
import cc.aoeiuv020.base.jar.pick
import cc.aoeiuv020.panovel.api.*
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.base.JsoupNovelContext
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.MalformedURLException
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

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
                                    Date(0)
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
        connection = requireNotNull(connection).also { it.request().removeCookie("_csrfToken") }
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

// 这类已经不用了，搞定dsl就删除，
@Suppress("ClassName", "unused")
class _Qidian : JsoupNovelContext() {
    override val site = NovelSite(
            name = "起点中文",
            baseUrl = "https://www.qidian.com",
            logo = "https://qidian.gtimg.com/qd/images/logo.dbed5.png"
    )

    override fun connectByNovelName(name: String): Connection {
        val key = URLEncoder.encode(name, "UTF-8")
        return connect(absUrl("/search?kw=$key"))
    }

    override fun getSearchResultList(root: Document): List<NovelItem> {
        return root.requireElements("#result-list > div > ul > li").map {
            /*
            <a href="//book.qidian.com/info/3548786" target="_blank" data-eid="qd_S05" data-bid="3548786" data-algrid="0.0.0">重生之<cite class="red-kw">都市</cite>修仙</a>
             */
            val a = it.requireElement("> div.book-mid-info > h4 > a", name = TAG_NOVEL_LINK)
            val name = a.text()
            val bookId = findBookId(a.href())
            val mid = it.requireElement("> div.book-mid-info")
            val author = mid.requireElement("> p.author", name = TAG_AUTHOR_NAME) { it.child(1).text() }
            logger.debug { "result $name.$author" }
            NovelItem(this, name, author, bookId)
        }
    }

    override fun getNextPage(root: Document): String? {
        val a = root.getElement("#page-container > div > ul > li > a.lbf-pagination-next")
                ?: return null
        val url = a.absHref()
        return url.takeIf { url.isNotEmpty() }
    }

    // 详情页域名和首页不一样，
    override val detailTemplate: String
        get() = "//book.qidian.com/info/%s"

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelDetail(root: Document): NovelDetail {
        val detail = root.requireElement("body > div.wrap > div.book-detail-wrap.center990")
        val information = detail.requireElement("> div.book-information.cf > div.book-info")
        val img = detail.requireElement("#bookImg > img", name = TAG_IMAGE) { it.absSrc() }
        val name = information.requireElement("> h1 > em", name = TAG_NOVEL_NAME) { it.text() }
        val author = information.requireElement("h1 > span", name = TAG_AUTHOR_NAME) { it.text().removeSuffix(" 著") }
        val intro = detail.getElement("div.book-intro > p") {
            it.ownTextList().joinToString("\n")
        }.toString()

        // 先从章节列表中解析更新时间，不存在就从小说详情解析，
        val update = root.getElement("#j-catalogWrap > div.volume-wrap > div:nth-last-child(1) > ul > li:nth-last-child(1) > a") {
            val (updateString) = it.title().pick("首发时间：(.*) 章节字数：.*")
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            sdf.parse(updateString)
        } ?: detail.getElement("div.book-state > ul > li.update > div > p.cf") { cf ->
            val updateString = cf.getElement("> em") { it.text() } ?: return@getElement null
            when {
            // 这个规则改过，不一定有哪些情况，无所谓，大不了不要这个时间，
                updateString.endsWith("小时前") -> {
                    val (hour) = updateString.pick("(\\d*)小时前")
                    Calendar.getInstance().run {
                        add(Calendar.HOUR_OF_DAY, -hour.toInt())
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        time
                    }
                }
                else -> {
                    val sdf = SimpleDateFormat("yyyy-MM-dd")
                    sdf.parse(updateString)
                }
            }
        }

        val bookId = findFirstOneInt(root.location())
        return NovelDetail(NovelItem(this, name, author, bookId), img, update, intro, bookId)
    }

    override val chapterTemplate: String
        get() = "$detailTemplate#Catalog"

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelChaptersAsc(extra: String): List<NovelChapter> {
        val bookId = findFirstOneInt(extra)
        // 用到的接口需要cookies中的_csrfToken参数，
        // 如果没有，就额外拿一遍详情页，取其中返回的_csrfToken，
        // TODO: 缓存cookies的话，_csrfToken不知道会不会过期，有必要测试下如果过期会拿到什么，
        val token = cookies["_csrfToken"] ?: run {
            response(connect(absUrl("/info/$bookId"))).cookie("_csrfToken")
        }
        val category = "https://book.qidian.com/ajax/book/category?_csrfToken=$token&bookId=$bookId"
        val categoryJson = response(connect(category)).body()
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
        // 是否是vip卷，判断一下分别处理，可能省点跳转的流量，
        var vipVolume = false
        return Gson().fromJson(categoryJson, JsonObject::class.java)
                .getAsJsonObject("data")
                .getAsJsonArray("vs").map {
                    it.asJsonObject.apply {
                        getAsJsonPrimitive("vS").asInt.let { vipVolume = it == 1 }
                    }.getAsJsonArray("cs").map {
                        it.asJsonObject.let {
                            val chapterName = it.getAsJsonPrimitive("cN").asString
                            // 免费章节的真实最终地址包含这个cU, 但是也可以拼接成vip章节的地址然后让它自动重定向，
                            val cU = if (!vipVolume) {
                                // 有用户反应这里出问题，但是没反馈清楚，直接把这字段改成不必要的，
                                // 最后发现不是这里的问题，是json被jsoup截断了，刚好停在了这里，
                                it.getAsJsonPrimitive("cU").asString
                            } else {
                                ""
                            }
                            val chapterId = it.getAsJsonPrimitive("id").asInt.toString()
                            val updateTime = try {
                                val uT = it.getAsJsonPrimitive("uT").asString
                                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                sdf.parse(uT)
                            } catch (e: Exception) {
                                Date(0)
                            }
                            // 用冒号分开，本想和网址path分隔符统一，但还是和旧版一致吧，
                            // 后面解析正文时分开判断，
                            NovelChapter(chapterName, "$bookId:$chapterId:$cU:$vipVolume", updateTime)
                        }
                    }
                }.reduce { acc, list ->
                    acc + list
                }
    }

    /**
     * 可能是冒号：分隔，
     */
    override val chapterIdRegex: Pattern =
            Pattern.compile("(\\d+):(\\d+):")

    // https://vipreader.qidian.com/chapter/3548786/343859470
    override val contentTemplate: String
        get() = "https://vipreader.qidian.com/chapter/%s"

    override fun findChapterId(extra: String): String {
        val (bookId, chapterId) = extra.pick(chapterIdRegex)
        return "$bookId/$chapterId"
    }

    override fun getNovelContentUrl(extra: String): String = try {
        val (_, _, cU, vipVolume) = extra.split(':')
        require(!vipVolume.toBoolean())
        require(cU.isNotBlank())
        // https://read.qidian.com/chapter/WCt0Tp3YhqQ1/4byHQO_QsHnwrjbX3WA1AA2
        "https://read.qidian.com/chapter/$cU"
    } catch (e: Exception) {
        try {
            super.getNovelContentUrl(extra)
        } catch (e: Exception) {
            // 兼容旧版，这是特别老的情况了，以前直接存的网页完整地址，
            extra
        }
    }

    override fun getNovelText(extra: String): NovelText = try {
        // 兼容旧版，也不知道多旧的，应该用不上了，传入的直接就是小说正文页面地址，
        // 如果extra是地址，走下面的split可能有问题，因为地址可能是https://有冒号，
        val url = URL(extra).toExternalForm()
        @Suppress("DEPRECATION")
        getNovelText(parse(connect(url)))
    } catch (e: MalformedURLException) {
        val args = extra.split(':')
        val bookId = args[0]
        val chapterId = args[1]
        val apiUrl = "https://m.qidian.com/majax/chapter/getChapterInfo?bookId=$bookId&chapterId=$chapterId"
        // 不删除这个Cookie就拿不到页面，或者把同样的参数放一份在get参数里，不知道起点怎么想的，
        val body = response(connect(apiUrl).also { it.request().removeCookie("_csrfToken") }).body()
        val json = Gson().fromJson(body, JsonObject::class.java)
        val content = json.getAsJsonObject("data")
                .getAsJsonObject("chapterInfo")
                .getAsJsonPrimitive("content")
                .asString
        NovelText(Jsoup.parse(content).select("p").ownTextList())
    }

    @Deprecated("由于特别废流量，已经废弃了，")
    override fun getNovelText(root: Document): NovelText {
        // 兼容以前的直接解析html,
        val query = if (root.location().contains("m.qidian.com")) {
            // 手机版页面，
            "#chapterContent > section > p"
        } else {
            // 电脑版页面，
            "div#j_chapterBox > div > div > div.read-content.j_readContent > p"
        }
        return NovelText(root.requireElements(query = query, name = TAG_CONTENT).ownTextList())
    }
}
