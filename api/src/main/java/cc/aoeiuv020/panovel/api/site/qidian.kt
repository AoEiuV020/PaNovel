package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.debug
import cc.aoeiuv020.base.jar.pick
import cc.aoeiuv020.panovel.api.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.net.URLEncoder
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESedeKeySpec
import javax.crypto.spec.IvParameterSpec

/**
 *
 * Created by AoEiuV020 on 2017.10.16-17:40:38.
 */
class Qidian : JsoupNovelContext() {
    override val site = NovelSite(
            name = "起点中文",
            baseUrl = "https://www.qidian.com",
            logo = "https://qidian.gtimg.com/qd/images/logo.dbed5.png"
    )

    override fun getNextPage(genre: NovelGenre): NovelGenre? {
        val root = request(genre.requester)
        val a = root.getElement("#page-container > div > ul > li > a.lbf-pagination-next")
                ?: return null
        val url = a.absHref()
        if (url.isEmpty()) return null
        return NovelGenre(genre.name, url)
    }

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelList(requester: Requester): List<NovelListItem> {
        val root = request(requester)
        return root.requireElements("#result-list > div > ul > li").map {
            val a = it.requireElement("> div.book-mid-info > h4 > a", name = TAG_NOVEL_LINK)
            val name = a.text()
            val url = a.absHref()
            val mid = it.requireElement("> div.book-mid-info")
            val author = mid.requireElement("> p.author", name = TAG_AUTHOR_NAME) { it.child(1).text() }
            val genre = mid.getElement("> p.author > a:nth-child(4)") { it.text() }
            val status = mid.getElement("> p.author > span") { it.text() }
            val introduction = mid.getElement("> p.intro") { it.text().trim() }
            val update = mid.getElement("> p.update") {
                val (updateString) = it.text().pick("最新更新 (.*)")
                updateString
            }
            val info = it.getElement("> div.book-right-info") { right ->
                val length = right.getElement("> div > p:nth-child(1) > span") { it.text() }
                val recommend = right.getElement("> div > p:nth-child(2) > span") { it.text() }
                val click = right.getElement("> div > p:nth-child(3) > span") { it.text() }
                "类型: $genre 更新: $update 状态: $status 长度: $length 推荐: $recommend 点击: $click 简介: $introduction"
            }.toString()
            logger.debug { "result $name.$author" }
            NovelListItem(NovelItem(this, name, author, url), info)
        }
    }

    override fun searchNovelName(name: String): NovelGenre {
        val key = URLEncoder.encode(name, "UTF-8")
        val url = "https://www.qidian.com/search?kw=$key"
        return NovelGenre(name, url)
    }

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelDetail(requester: Requester): NovelDetail {
        val root = if (requester.url.startsWith("https://m.qidian.com/book/")) {
            val pcUrl = "https://book.qidian.com/info/" + requester.url.removePrefix("https://m.qidian.com/book/")
            request(Requester(pcUrl))
        } else {
            request(requester)
        }
        val detail = root.requireElement("body > div.wrap > div.book-detail-wrap.center990")
        val information = detail.requireElement("> div.book-information.cf > div.book-info")
        val img = detail.requireElement("#bookImg > img", name = TAG_IMAGE) { it.absSrc() }
        val name = information.requireElement("> h1 > em", name = TAG_NOVEL_NAME) { it.text() }
        val author = information.requireElement("h1 > span", name = TAG_AUTHOR_NAME) { it.text().removeSuffix(" 著") }
        val intro = detail.getElement("div.book-intro > p") {
            it.textNodes().joinToString("\n") {
                it.toString().trim()
            }
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
        } ?: Date(0)

        val chapterPageUrl = requester.url
        return NovelDetail(NovelItem(this, name, author, requester), img, update, intro, chapterPageUrl)
    }

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelChaptersAsc(requester: Requester): List<NovelChapter> {
        val token = cookies["_csrfToken"] ?: run {
            response(requester).cookie("_csrfToken")
        }
        val bookId = requester.url.removePrefix("https://book.qidian.com/info/")
        val category = "https://book.qidian.com/ajax/book/category?_csrfToken=$token&bookId=$bookId"
        val categoryJson = response(category).body()
        var vipVolume = false
        return Gson().fromJson(categoryJson, JsonObject::class.java)
                .getAsJsonObject("data")
                .getAsJsonArray("vs").map {
                    it.asJsonObject.apply { getAsJsonPrimitive("vS").asInt.let { vipVolume = it == 1 } }
                            .getAsJsonArray("cs").map {
                                it.asJsonObject.let {
                                    val chapterName = it.getAsJsonPrimitive("cN").asString
                                    val cU = try {
                                        // 有用户反应这里出问题，但是没反馈清楚，直接把这字段改成不必要的，
                                        it.getAsJsonPrimitive("cU").asString
                                    } catch (_: Exception) {
                                        ""
                                    }
                                    val chapterId = it.getAsJsonPrimitive("id").asInt.toString()
                                    val uT = it.getAsJsonPrimitive("uT").asString
                                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                    val updateTime = sdf.parse(uT)
                                    if (!vipVolume) {
                                        // 免费卷，
                                        NovelChapter(chapterName, FreeRequester(bookId, chapterId, cU), updateTime)
                                    } else {
                                        // VIP卷，
                                        NovelChapter(chapterName, VipRequester(bookId, chapterId), updateTime)
                                    }
                                }
                            }
                }.reduce { acc, list ->
                    acc + list
                }
    }

    /**
     * 缓存分段的正则规则，
     *  <p> 后面可能是半角空格或全角空格，
     */
    private val paragraphSplitRegex = Regex("<p>[　 ]*")

    override fun getNovelText(requester: Requester): NovelText {
        if (requester is MobileRequester) {
            val json = Gson().fromJson(
                    response(requester)
                            .body(),
                    JsonObject::class.java)
            val content = json.getAsJsonObject("data")
                    .getAsJsonObject("chapterInfo")
                    .getAsJsonPrimitive("content")
                    .asString
            return NovelText(content.split(paragraphSplitRegex).drop(1))
        }
        // 兼容以前的直接解析html,
        val root = request(requester)
        val query = if (requester is VipRequester) {
            // 这部分已经废了，
            "#chapterContent > section > p"
        } else {
            "div#j_chapterBox > div > div > div.read-content.j_readContent > p"
        }
        val textList = root.requireElements(query, name = TAG_CONTENT).map {
            it.text().trim()
        }.dropLastWhile(String::isBlank)
        return NovelText(textList)
    }

    override fun getNovelItem(url: String): NovelItem {
        val bookId = findBookId(url)
        val detailUrl = "https://book.qidian.com/info/$bookId"
        return super.getNovelItem(detailUrl)
    }

    abstract class MobileRequester(bookId: String, chapterId: String, cU: String) : Requester("$bookId:$chapterId:$cU") {
        companion object {
            fun splitExtra(extra: String): List<String> = extra.split(':')
        }

        private val apiUrl = "https://m.qidian.com/majax/chapter/getChapterInfo?bookId=$bookId&chapterId=$chapterId"

        override fun connect(): Connection = Jsoup.connect(apiUrl)
        override fun doBeforeExecute(conn: Connection): Connection = conn.apply {
            // 不删除就拿不到页面，或者把同样的参数放一份在get参数里，不知道起点怎么想的，
            request().removeCookie("_csrfToken")
        }
    }

    /**
     * cU没必要，只是希望能打开电脑版最终页面，
     * 没有的话vipreader也能打开，
     */
    class FreeRequester(bookId: String, chapterId: String, cU: String = "") : MobileRequester(bookId, chapterId, cU) {
        companion object {
            @JvmStatic
            fun new(extra: String): FreeRequester {
                val (bookId, chapterId, cU) = splitExtra(extra)
                return FreeRequester(bookId, chapterId, cU)
            }
        }

        override val url = if (cU.isEmpty()) {
            "https://vipreader.qidian.com/chapter/$bookId/$chapterId"
        } else {
            "https://read.qidian.com/chapter/$cU"
        }
    }

    /**
     * Vip获取方式失效了已经，
     * 2018.5.3 19:00左右，
     */
    class VipRequester(bookId: String, chapterId: String) : MobileRequester(bookId, chapterId, "") {
        companion object {
            @JvmStatic
            fun new(extra: String): VipRequester {
                val pattern = Pattern.compile("https://vipreader.qidian.com/chapter/(\\d*)/(\\d*)")
                return if (pattern.matcher(extra).matches()) {
                    // 以前直接存地址的缓存也要兼容，之后可以作废，
                    val (bookId, chapterId) = extra.pick(pattern)
                    VipRequester(bookId, chapterId)
                } else {
                    val (bookId, chapterId) = splitExtra(extra)
                    VipRequester(bookId, chapterId)
                }
            }

            private var cachedId = newId()
            private var downloadCount = 0
            private fun newId() = qidianMd5Hex(System.currentTimeMillis().toString() + Math.random().toString())
        }

        override val url = "https://vipreader.qidian.com/chapter/$bookId/$chapterId"

        override fun connect(): Connection {
            val deviceId = "878788848187878"
            if (++downloadCount > 2000) {
                downloadCount = 0
                cachedId = newId() // 不锁也无所谓，顶多多换了一次id,
            }
            val id = cachedId
            val urlMd5 = qidianMd5Hex(url)
            val plain = "QDLite!@#$%|${System.currentTimeMillis()}|$deviceId|$id|1|1.0.0|1000147|$urlMd5"
            val sign = URLEncoder.encode(qidianDes3(plain).replace(" ", ""), "ascii")
            return super.connect().cookie("QDSign", sign)
        }
    }
}

/**
 * 反编译自起点畅读，不要动不要用，
 */
private fun qidianMd5Hex(str: String): String {
    val digest = MessageDigest.getInstance("MD5").digest(str.toByteArray(charset("UTF-8")))
    val stringBuilder = StringBuilder(digest.size * 2)
    for (b in digest) {
        if (b.toInt() and 255 < 16) {
            stringBuilder.append("0")
        }
        stringBuilder.append(Integer.toHexString(b.toInt() and 255))
    }
    return stringBuilder.toString()
}

/**
 * 反编译自起点畅读，不要动不要用，
 */
private fun qidianDes3(str: String): String {
    val generateSecret = SecretKeyFactory.getInstance("desede").generateSecret(DESedeKeySpec("JVYW9BWG7XJ98B3W34RT33B3".toByteArray()))
    val instance = Cipher.getInstance("desede/CBC/PKCS5Padding")
    instance.init(1, generateSecret, IvParameterSpec("01234567".toByteArray()))
    return qidianBase64(instance.doFinal(str.toByteArray(charset("utf-8"))))
}

/**
 * 反编译自起点畅读，不要动不要用，
 */
private fun qidianBase64(bArr: ByteArray): String {
    val a = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray()
    val length = bArr.size
    val stringBuffer = StringBuilder(bArr.size * 3 / 2)
    val i = length - 3
    var i2 = 0
    var i3 = 0
    while (i3 <= i) {
        var i4 = bArr[i3].toInt() and 255 shl 16 or (bArr[i3 + 1].toInt() and 255 shl 8) or (bArr[i3 + 2].toInt() and 255)
        stringBuffer.append(a[i4 shr 18 and 63])
        stringBuffer.append(a[i4 shr 12 and 63])
        stringBuffer.append(a[i4 shr 6 and 63])
        stringBuffer.append(a[i4 and 63])
        i4 = i3 + 3
        i3 = i2 + 1
        if (i2 >= 14) {
            stringBuffer.append(" ")
            i3 = 0
        }
        i2 = i3
        i3 = i4
    }
    if (i3 == length - 2) {
        i3 = bArr[i3 + 1].toInt() and 255 shl 8 or (bArr[i3].toInt() and 255 shl 16)
        stringBuffer.append(a[i3 shr 18 and 63])
        stringBuffer.append(a[i3 shr 12 and 63])
        stringBuffer.append(a[i3 shr 6 and 63])
        stringBuffer.append("=")
    } else if (i3 == length - 1) {
        i3 = bArr[i3].toInt() and 255 shl 16
        stringBuffer.append(a[i3 shr 18 and 63])
        stringBuffer.append(a[i3 shr 12 and 63])
        stringBuffer.append("==")
    }
    return stringBuffer.toString()
}
