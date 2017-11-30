package cc.aoeiuv020.panovel.api

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat


/**
 *
 * Created by AoEiuV020 on 2017.11.30-17:49:36.
 */
class Dmzz : NovelContext() {
    private val site = NovelSite(
            name = "动漫之家",
            baseUrl = "http://q.dmzj.com",
            logo = "http://m.dmzj.com/images/head_logo.gif"
    )

    override fun getNovelSite(): NovelSite = site

    override fun getGenres(): List<NovelGenre> {
        val root = request("http://q.dmzj.com/search.shtml")
        val elements = root.select("#search_tags > div.se2 > p > a")
        return elements.map { a ->
            NovelGenre(a.text(), GenreListRequester(a.absHref()))
        }
    }

    override fun getNextPage(genre: NovelGenre): NovelGenre? {
        return null
    }

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelList(requester: ListRequester): List<NovelListItem> {
        val arr: List<DmzzNovelItem> = if (requester is SearchListRequester) {
            response(connect(requester).ignoreContentType(true)).body()
        } else {
            val (genreName) = requester.url.pick(".*tags/(\\S*)\\.shtml")
            val jsUrl = "http://q.dmzj.com/tags/js/$genreName.js"
            val conn = connect(jsUrl).ignoreContentType(true)
            response(conn).body()
        }.let { js ->
            val json = js.dropWhile { it != '[' }
                    .dropLastWhile { it != ']' }
            Gson().fromJson(json, object : TypeToken<List<DmzzNovelItem>>() {}.type)
        }
        return arr.map { dmzz ->
            val info = dmzz.mIntro ?: dmzz.description ?: null.toString()
            NovelListItem(NovelItem(this, dmzz.fullName, dmzz.author, "http://q.dmzj.com" + dmzz.lnovelUrl), info)
        }
    }

    data class DmzzNovelItem(
            @SerializedName("author") val author: String, //仁木英之
            @SerializedName("image_url") val imageUrl: String, //../img/webpic/4/pupuxianrenqiansuishaonv.jpg
            @SerializedName("full_name") val fullName: String, //仆仆仙人千岁少女
            @SerializedName("lnovel_name") val lnovelName: String, //仆仆仙人千岁少女
            @SerializedName("fullc_name") val fullcName: String, //第一卷
            @SerializedName("last_chapter_name") val lastChapterName: String, //第一卷
            @SerializedName("lnovel_url") val lnovelUrl: String, ///1473/index.shtml
            @SerializedName("last_chapter_url") val lastChapterUrl: String, ///1473/5402/36527.shtml
            @SerializedName("m_image_url") val mImageUrl: String, //http://xs.dmzj.com/img/webpic/4/pupuxianrenqiansuishaonv.jpg
            @SerializedName("m_intro") val mIntro: String?, //　　一日，神仙降临在我眼前。却是个辛辣又大胆的千岁……美少女？！　...
            @SerializedName("description") val description: String?,
            @SerializedName("status") val status: String //[<span class="red1_font12">完</span>]
    )

    override fun searchNovelName(name: String): NovelGenre {
        val key = URLEncoder.encode(name, "UTF-8")
        val url = "http://s.acg.dmzj.com/lnovelsum/search.php?s=$key"
        return NovelSearch(name, url)
    }

    override fun check(url: String): Boolean {
        return super.check(url) ||
                URL(url).host == "s.acg.dmzj.com"
    }

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelDetail(requester: DetailRequester): NovelDetail {
        val root = request(requester)
        val con = root.select("body > div.main > div > div.pic > div ").first()

        val img = root.select("#cover_pic").first().src()
        val name = con.select("> h3").first().text()
        val (author) = con.select("> p:nth-child(2)").first().text()
                .pick("作者：(\\S*)")
        val (updateString) = con.select("> p:nth-child(5)").first().text()
                .pick("更新：(.*)")
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val update = sdf.parse(updateString)
        val info = ""
        val chapterPageUrl = requester.url
        return NovelDetail(NovelItem(this, name, author, requester), img, update, info, chapterPageUrl)
    }

    override fun getNovelChaptersAsc(requester: ChaptersRequester): List<NovelChapter> {
        val root = request(requester)
        val text = root.select("#list_block > script").first().html()
        val regex = Regex(".*chapter_list\\[\\d*\\]\\[\\d*\\] = '<a href=\"([^\"]*)\".*>(\\S*)</a>'.*;.*")
        return text.lines().filter { it.matches(regex) }.map {
            val (url, name) = it.pick(regex.toPattern())
            NovelChapter(name, "http://q.dmzj.com" + url)
        }
    }

    override fun getNovelText(requester: TextRequester): NovelText {
        val root = request(requester)
        val script = root.select("head > script:nth-child(10)").first().html()
        val (json) = script.pick("var g_chapter_pages_url = (\\[.*\\]);")
        val urlList: List<String> = Gson().fromJson(json, object : TypeToken<List<String>>() {}.type)
        val textList = urlList.map {
            val url = if (it.isEmpty()) {
                requester.url
            } else {
                "http://q.dmzj.com" + it
            }
            request(url).select("p").map {
                it.text().trim()
            }.dropLastWhile(String::isBlank)
        }.reduce { acc, list ->
            acc + list
        }
        return NovelText(textList)
    }
}
