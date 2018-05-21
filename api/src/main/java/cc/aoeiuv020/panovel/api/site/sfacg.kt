package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.pick
import cc.aoeiuv020.panovel.api.*
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.base.JsoupNovelContext
import org.jsoup.Connection
import org.jsoup.nodes.Document
import org.jsoup.nodes.TextNode
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.regex.Pattern

/**
 *
 * Created by AoEiuV020 on 2018.03.06-18:10:46.
 */
class Sfacg : DslJsoupNovelContext() { init {
    site {
        name = "SF轻小说"
        baseUrl = "http://book.sfacg.com"
        logo = "http://rs.sfacg.com/images/sflogo.gif"
    }
    search {
        get {
            url = "http://s.sfacg.com/?Key=$it&S=1&SS=0"
        }
        document {
            /*
            <ul style="width:100%">
                <li class="Conjunction">
                    <img src="http://rs.sfacg.com/web/novel/images/NovelCover/Small/2016/07/5fe29c4c-d774-4e12-b4e3-578814f51fec.jpg" id="SearchResultList1___ResultList_Cover_0" border="0" alt="吸血萝莉在都市" width="80" height="100"></li>
                <li><strong class="F14PX"><a href="http://book.sfacg.com/Novel/44856" id="SearchResultList1___ResultList_LinkInfo_0" class="orange_link2">吸血萝莉在都市</a></strong>
                    <br> 综合信息： 那一片宁静/2018/5/9 20:11:26
                    <br> 　一个高中毕业的公子哥因通宵玩电脑而猝死，醒来时却发现自己成了萝莉...... 　　但是这个萝莉有什么不可告人的秘密？结局将会如何？ 　　本萝莉又软又萌，喜欢软妹子的读者老爷千万别错过。 　　－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－顺便带着点小傲娇哟～～ PS：不会上架的QWQ
                </li>
            </ul>
             */
            items("#form1 > table.comic_cover.Height_px22.font_gray.space10px > tbody > tr > td > ul") {
                name("li > strong > a")
                author("> li:nth-child(2)", block = pickString("综合信息： ([^/]*)/"))
            }
        }
    }
    detailTemplate = "/Novel/%s/"
    detail {
        document {
            val div = element("div.wrap > div.d-summary > div.summary-content")
            novel {
                name("> h1 > span.text", parent = div)
                author("> div.count-info.clearfix > div.author-info > div.author-name > span", div)
            }
            image("#hasTicket > div.left-part > div > div.pic > a > img")
            introduction("> p", parent = div)
            update("> div.count-info.clearfix > div.count-detail span:nth-child(4)", format = "yyyy/MM/dd HH:mm:ss", block = pickString("更新：(.*)"))
        }
    }
    chapterTemplate = "/Novel/%s/MainIndex/"
    chapters {
        document {
            items("div.story-catalog > div.catalog-list > ul > li > a") {
                name = root.text()
                extra = root.href()
            }
        }
    }
    getNovelContentUrl {
        // vip章节和普通章节规则不一致，不统一处理，
        try {
            // http://book.sfacg.com/Novel/123589/204084/1887037/
            val bookId = it.pick(firstThreeIntPattern).first()
            "http://book.sfacg.com/Novel/$bookId/"
        } catch (e: Exception) {
            // http://book.sfacg.com/vip/c/1725750/
            val bookId = it.pick(firstIntPattern).first()
            "http://book.sfacg.com/vip/c/$bookId/"
        }
    }
    content {
        document {
            // vip章节仅有的一行没有包在p里，
            // 普通章节有"#ChapterBody > p",
            items("#ChapterBody")
        }
    }
}
}

// 这类已经不用了，搞定dsl就删除，
@Suppress("ClassName", "unused")
class _Sfacg : JsoupNovelContext() {

    override val site = NovelSite(
            name = "SF轻小说",
            baseUrl = "http://book.sfacg.com",
            logo = "http://rs.sfacg.com/images/sflogo.gif"
    )

    override fun connectByNovelName(name: String): Connection {
        val key = URLEncoder.encode(name, "UTF-8")
        return connect("http://s.sfacg.com/?Key=$key&S=1&SS=0")
    }

    override fun getSearchResultList(root: Document): List<NovelItem> {
        return root.requireElements("#form1 > table.comic_cover.Height_px22.font_gray.space10px > tbody > tr > td > ul").map {
            val a = it.requireElement("li > strong > a", name = TAG_NOVEL_LINK)
            val name = a.text()
            val bookId = findBookId(a.href())
            val li = it.requireElement("> li:nth-child(2)")
            val size = li.childNodeSize()
            val all = (li.childNode(size - 3) as TextNode).wholeText.trim()
            val (author, _) = all.pick("综合信息： ([^/]*)/(.*)")
            NovelItem(this, name, author, bookId)
        }
    }

    override val detailTemplate: String
        get() = "/Novel/%s/"

    @SuppressWarnings("SimpleDateFormat")
    override fun getNovelDetail(root: Document): NovelDetail {
        val img = root.requireElement("#hasTicket > div.left-part > div > div.pic > a > img", TAG_IMAGE) { it.src() }
        val div = root.requireElement("div.wrap > div.d-summary > div.summary-content")
        val name = div.requireElement("> h1 > span.text", TAG_NOVEL_NAME) { it.text() }
        val author = div.requireElement("> div.count-info.clearfix > div.author-info > div.author-name > span", TAG_AUTHOR_NAME) { it.text() }
        val intro = div.getElement("> p") {
            it.ownTextList().joinToString("\n")
        }.toString()

        val update = div.getElements("> div.count-info.clearfix > div.count-detail span") {
            val (updateString) = it[3].text().pick("更新：(.*)")
            val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
            sdf.parse(updateString)
        }

        val bookId = findFirstOneInt(root.location())
        return NovelDetail(NovelItem(this, name, author, bookId), img, update, intro, bookId)
    }

    override val chapterTemplate: String?
        get() = "/Novel/%s/MainIndex/"

    override fun getNovelChaptersAsc(root: Document): List<NovelChapter> {
        return root.requireElements("div.story-catalog > div.catalog-list > ul > li > a", TAG_CHAPTER_LINK).map { a ->
            NovelChapter(a.title(), a.path())
        }
    }

    override val chapterIdRegex: Pattern
        get() = firstThreeIntPattern

    override val contentTemplate: String?
        get() = "/Novel/%s/"

    override fun getNovelText(extra: String): NovelText = try {
        super.getNovelText(extra)
    } catch (e: Exception) {
        getNovelText(parse(extra))
    }

    override fun getNovelText(root: Document): NovelText {
        // vip章节仅有的一行没有包在p里，
        // 普通章节有"#ChapterBody > p",
        return NovelText(root.requireElement("#ChapterBody", TAG_CONTENT).textList())
    }
}
