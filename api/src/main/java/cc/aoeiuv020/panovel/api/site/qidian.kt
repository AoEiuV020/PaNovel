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
class Qidian : NovelContext() {
    private val site = NovelSite(
            name = "起点中文",
            baseUrl = "https://www.qidian.com/",
            logo = "https://qidian.gtimg.com/qd/images/logo.dbed5.png"
    )

    override fun getNovelSite(): NovelSite = site

    override fun getGenres(): List<NovelGenre> {
        val map = linkedMapOf(-1 to linkedMapOf("全部" to -1),
                21 to linkedMapOf("东方玄幻" to 8,
                        "异世大陆" to 73,
                        "王朝争霸" to 58,
                        "高武世界" to 78),
                1 to linkedMapOf("现代魔法" to 38,
                        "剑与魔法" to 62,
                        "史诗奇幻" to 201,
                        "黑暗幻想" to 202,
                        "历史神话" to 20092,
                        "另类幻想" to 20093),
                2 to linkedMapOf("传统武侠" to 5,
                        "武侠幻想" to 30,
                        "国术无双" to 206,
                        "古武未来" to 20099,
                        "武侠同人" to 20100),
                22 to linkedMapOf("修真文明" to 18,
                        "幻想修仙" to 44,
                        "现代修真" to 64,
                        "神话修真" to 207,
                        "古典仙侠" to 20101),
                4 to linkedMapOf("都市生活" to 12,
                        "恩怨情仇" to 16,
                        "异术超能" to 74,
                        "青春校园" to 130,
                        "娱乐明星" to 151,
                        "官场沉浮" to 152,
                        "商战职场" to 153),
                15 to linkedMapOf("社会乡土" to 20104,
                        "生活时尚" to 20105,
                        "文学艺术" to 20106,
                        "成功励志" to 20107,
                        "青春文学" to 20108,
                        "爱情婚姻" to 6,
                        "现实百态" to 209),
                6 to linkedMapOf("军旅生涯" to 54,
                        "军事战争" to 65,
                        "战争幻想" to 80,
                        "抗战烽火" to 230,
                        "谍战特工" to 231),
                5 to linkedMapOf("架空历史" to 22,
                        "秦汉三国" to 48,
                        "上古先秦" to 220,
                        "历史传记" to 32,
                        "两晋隋唐" to 222,
                        "五代十国" to 223,
                        "两宋元明" to 224,
                        "清史民国" to 225,
                        "外国历史" to 226,
                        "民间传说" to 20094),
                7 to linkedMapOf("电子竞技" to 7,
                        "虚拟网游" to 70,
                        "游戏异界" to 240,
                        "游戏系统" to 20102,
                        "游戏主播" to 20103),
                8 to linkedMapOf("篮球运动" to 28,
                        "体育赛事" to 55,
                        "足球运动" to 82),
                9 to linkedMapOf("古武机甲" to 21,
                        "未来世界" to 25,
                        "星际文明" to 68,
                        "超级科技" to 250,
                        "时空穿梭" to 251,
                        "进化变异" to 252,
                        "末世危机" to 253),
                10 to linkedMapOf("恐怖惊悚" to 26,
                        "灵异鬼怪" to 35,
                        "悬疑侦探" to 57,
                        "寻墓探险" to 260,
                        "风水秘术" to 20095),
                12 to linkedMapOf("变身入替" to 10,
                        "原生幻想" to 60,
                        "青春日常" to 66,
                        "衍生同人" to 281,
                        "搞笑吐槽" to 282),
                20076 to linkedMapOf("诗歌散文" to 20097,
                        "人物传记" to 20098,
                        "影视剧本" to 20075,
                        "评论文集" to 20077,
                        "生活随笔" to 20078,
                        "美文游记" to 20079,
                        "儿童文学" to 20081,
                        "短篇小说" to 20096))
        return map.map {
            val chanId = it.key
            it.value.map {
                val name = it.key
                val subCateId = it.value
                NovelGenre(name, "https://www.qidian.com/all?chanId=$chanId&subCateId=$subCateId&orderId=&page=1&style=1&pageSize=20&siteid=1&hiddenField=0")
            }
        }.reduce { acc, list ->
            acc + list
        }
    }

    override fun getNextPage(genre: NovelGenre): NovelGenre? {
        if (genre.requester is SearchListRequester) {
            return null
        }
        val root = request(genre.requester)
        val a = root.select("#page-container > div > ul > li > a.lbf-pagination-next").first() ?: return null
        val url = a.absHref()
        if (url.isEmpty()) return null
        return NovelGenre(genre.name, url)
    }

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelList(requester: ListRequester): List<NovelListItem> {
        val root = request(requester)
        return if (requester is SearchListRequester) {
            root.select("#result-list > div > ul > li").map {
                val a = it.select("> div.book-mid-info > h4 > a").first()
                val name = a.text()
                val url = a.absHref()
                val mid = it.select("> div.book-mid-info").first()
                val author = mid.select("> p.author").first().child(1).text()
                val genre = mid.select("> p.author > a:nth-child(4)").first().text()
                val status = mid.select("> p.author > span").first().text()
                val introduction = mid.select("> p.intro").first().text().trim()
                val (update) = mid.select("> p.update").first().text().pick("最新更新 (.*)")
                val right = it.select("> div.book-right-info").first()
                val length = right.select("> div > p:nth-child(1) > span").first().text()
                val recommend = right.select("> div > p:nth-child(2) > span").first().text()
                val click = right.select("> div > p:nth-child(3) > span").first().text()
                val info = "类型: $genre 更新: $update 状态: $status 长度: $length 推荐: $recommend 点击: $click 简介: $introduction"
                logger.debug { "result $name.$author" }
                NovelListItem(NovelItem(this, name, author, url), info)
            }
        } else root.select("body > div.wrap > div.all-pro-wrap.box-center.cf > div.main-content-wrap.fl > div.all-book-list > div > ul > li").map {
            val a = it.select("> div.book-mid-info > h4 > a").first()
            val name = a.text()
            val url = a.absHref()
            val mid = it.select("> div.book-mid-info").first()
            val author = mid.select("> p.author > a.name").first().text()
            val genre = mid.select("> p.author > a.go-sub-type").first().text()
            val status = mid.select("> p.author > span").first().text()
            // 长度拿不下了，上了字体反爬，
            val introduction = mid.select("> p.intro").first().text().trim()
            val info = "类型: $genre 状态: $status 简介: $introduction"
            NovelListItem(NovelItem(this, name, author, url), info)
        }
    }

    override fun searchNovelName(name: String): NovelGenre {
        val key = URLEncoder.encode(name, "UTF-8")
        val url = "https://www.qidian.com/search?kw=$key"
        return NovelSearch(name, url)
    }

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelDetail(requester: DetailRequester): NovelDetail {
        val root = if (requester.url.startsWith("https://m.qidian.com/book/")) {
            val pcUrl = "https://book.qidian.com/info/" + requester.url.removePrefix("https://m.qidian.com/book/")
            request(DetailRequester(pcUrl))
        } else {
            request(requester)
        }
        val detail = root.select("body > div.wrap > div.book-detail-wrap.center990").first()
        val information = detail.select("> div.book-information.cf > div.book-info").first()
        val img = detail.select("#bookImg > img").first().absSrc()
        val name = information.select("> h1 > em").first().text()
        val author = information.select("h1 > span").first().text().removeSuffix(" 著")
        val info = detail.select("div.book-intro > p").first().textNodes().joinToString("\n") {
            it.toString().trim()
        }

        val cf = detail.select("div.book-state > ul > li.update > div > p.cf").first()

        val lastChapterElement = root.select("#j-catalogWrap > div.volume-wrap > div:nth-last-child(1) > ul > li:nth-last-child(1) > a").first()
        val update = if (lastChapterElement != null) {
            val (updateString) = lastChapterElement.title().pick("首发时间：(.*) 章节字数：.*")
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            sdf.parse(updateString)
        } else {
            val updateString = cf.select("> em").first().text()
            try {
                when {
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
            } catch (_: Exception) {
                // 这种分段判断不靠谱，以防万一，不要因为更新时间就看不了小说了，
                Date(0)
            }
        }

        val chapterPageUrl = requester.url
        return NovelDetail(NovelItem(this, name, author, requester), img, update, info, chapterPageUrl)
    }

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelChaptersAsc(requester: ChaptersRequester): List<NovelChapter> {
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

    override fun getNovelText(requester: TextRequester): NovelText {
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
        val textList = root.select(query).map {
            it.text().trim()
        }.dropLastWhile(String::isBlank)
        return NovelText(textList)
    }

    override fun getNovelItem(url: String): NovelItem {
        val bookId = findBookId(url)
        val detailUrl = "https://book.qidian.com/info/$bookId"
        return super.getNovelItem(detailUrl)
    }

    abstract class MobileRequester(bookId: String, chapterId: String, cU: String) : TextRequester("$bookId:$chapterId:$cU") {
        companion object {
            fun splitExtra(extra: String): List<String> = extra.split(':')
        }

        private val apiUrl = "https://m.qidian.com/majax/chapter/getChapterInfo?bookId=$bookId&chapterId=$chapterId"

        override fun connect(): Connection = Jsoup.connect(apiUrl)
        override fun doBeforeExecute(conn: Connection): Connection = conn.apply {
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
