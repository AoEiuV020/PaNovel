package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.pick
import cc.aoeiuv020.panovel.api.*
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import org.jsoup.Connection
import org.jsoup.nodes.Document
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat


/**
 *
 * Created by AoEiuV020 on 2017.11.30-17:49:36.
 */
class Dmzz : JsoupNovelContext() {
    override val site = NovelSite(
            name = "动漫之家",
            baseUrl = "http://q.dmzj.com",
            logo = "http://m.dmzj.com/images/head_logo.gif"
    )

    override fun connectByNovelName(name: String): Connection {
        val key = URLEncoder.encode(name, "UTF-8")
        return connect("http://s.acg.dmzj.com/lnovelsum/search.php?s=$key")
    }

    override fun searchNovelName(name: String): List<NovelItem> {
        val arr: List<DmzzNovelItem> = response(connect(absUrl(name)).ignoreContentType(true)).body().let { js ->
            val json = js.dropWhile { it != '[' }
                    .dropLastWhile { it != ']' }
            Gson().fromJson(json, object : TypeToken<List<DmzzNovelItem>>() {}.type)
        }
        return arr.map { dmzz ->
            NovelItem(this, dmzz.fullName, dmzz.author,
                    // 相对路径，"../"开头，jsoup没有自动处理这样的的，
                    // TODO: URL有处理，待测试，
                    dmzz.lnovelUrl.removePrefix(".."))
        }
    }

    data class DmzzNovelItem(
            @SerializedName("author") val author: String, //仁木英之
            @SerializedName("image_url") val imageUrl: String, //http://xs.dmzj.com/img/webpic/11/0005O.jpg
            @SerializedName("full_name") val fullName: String, //仆仆仙人千岁少女
            @SerializedName("lnovel_name") val lnovelName: String, //仆仆仙人千岁少女
            @SerializedName("fullc_name") val fullcName: String, //第一卷
            @SerializedName("last_chapter_name") val lastChapterName: String, //第一卷
            @SerializedName("lnovel_url") val lnovelUrl: String, //../4/index.shtml
            @SerializedName("last_chapter_url") val lastChapterUrl: String, ///../4/28/141.shtml
            @SerializedName("m_image_url") val mImageUrl: String, //http://xs.dmzj.com/img/webpic/4/pupuxianrenqiansuishaonv.jpg
            @SerializedName("m_intro") val mIntro: String?, //　　一日，神仙降临在我眼前。却是个辛辣又大胆的千岁……美少女？！　...
            @SerializedName("description") val description: String?,
            @SerializedName("status") val status: String //[<span class="red1_font12">完</span>]
    )

    override fun check(url: String): Boolean {
        return super.check(url) ||
                URL(url).host == "s.acg.dmzj.com"
    }

    override val detailTemplate: String
        get() = "/%s/index.shtml"

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelDetail(root: Document): NovelDetail {
        val con = root.requireElement(query = "body > div.main > div > div.pic > div ")

        val img = root.requireElement(query = "#cover_pic", name = TAG_IMAGE) { it.src() }
        val name = con.requireElement(query = "> h3", name = TAG_NOVEL_NAME) { it.text() }
        val (author) = con.requireElement(query = "> p:nth-child(2)", name = TAG_AUTHOR_NAME) {
            it.text().pick("作者：(\\S*)")
        }
        val update = con.getElement(query = "> p:nth-child(5)") {
            val (updateString) = it.text().pick("更新：(.*)")
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            sdf.parse(updateString)
        }
        val intro = ""

        val bookId = findBookId(root.location())
        return NovelDetail(NovelItem(this, name, author, bookId), img, update, intro, bookId)
    }

    override fun getNovelChaptersAsc(root: Document): List<NovelChapter> {
        val regex = Regex(".*chapter_list\\[\\d*\\]\\[\\d*\\] = '<a href=\"([^\"]*)\".*>(.*)</a>'.*;.*")
        return root.requireElement(query = "#list_block > script", name = TAG_CHAPTER_LINK) {
            it.html().lines().filter { it.matches(regex) }.map {
                val (url, name) = it.pick(regex.toPattern())
                NovelChapter(name, url)
            }
        }
    }

    override fun getNovelText(root: Document): NovelText {
        val textList = root.requireElement(query = "head > script:nth-child(10)", name = TAG_CONTENT) {
            val (json) = it.html().pick("var g_chapter_pages_url = (\\[.*\\]);")
            val urlList: List<String> = Gson().fromJson(json, object : TypeToken<List<String>>() {}.type)
            urlList.map {
                val url = if (it.isEmpty()) {
                    root.location()
                } else {
                    absUrl(it)
                }
                parse(url).requireElements(query = "p")
                        .dropLastWhile { it.className() == "zlist" }
                        .flatMap {
                            // 有的只有一个p，
                            // http://q.dmzj.com/2013/7335/54663.shtml
                            it.textList()
                        }
            }.reduce { acc, list ->
                acc + list
            }
        }
        return NovelText(textList)
    }
}
