package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.ownLinesString
import cc.aoeiuv020.gson.GsonUtils
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstThreeIntPattern
import com.google.gson.Gson
import com.google.gson.JsonObject

/**
 * Created by AoEiuV020 on 2018.06.08-19:09:23.
 */
class Ttkan : DslJsoupNovelContext() {init {
    site {
        name = "天天看小说"
        baseUrl = "https://cn.ttkan.co"
        logo = "text://天天看小说/?fc=ffffff&bc=1b2631"
    }
    search {
        get {
            url = "/novel/search"
            data {
                "q" to it
            }
        }
        document {
            items("div.frame_body > div.pure-g > div > ul") {
                name("> li:nth-child(1) > a")
                // 有作者名字空白，https://hk.ttkan.co/novel/chapters/yuanworuxingjunruyue
                author("> li:nth-child(2)", block = pickString("作者：(\\S*)"))
            }
        }
    }
    // https://www.ttkan.co/novel/chapters/shisiruguiweijunzi-pingceng
    bookIdRegex = "/novel/chapters/(.*)"
    detailPageTemplate = "/novel/chapters/%s"
    detail { _ ->
        document {
            novel {
                name("div.pure-g.novel_info > div:nth-child(2) > ul > li:nth-child(1) > h1")
                author("div.pure-g.novel_info > div:nth-child(2) > ul > li:nth-child(2) > a")
            }
            image("div.pure-g.novel_info > div:nth-child(1) > a > amp-img")
            introduction("div.description > div")
        }
    }
    chapters {
        val bookId = findBookId(it)
        get {
            url =
                "https://www.ttkan.co/api/nq/amp_novel_chapters?language=cn&novel_id=$bookId&__amp_source_origin=https%3A%2F%2Fwww.ttkan.co"
        }
        response {
            gson.fromJson(it, JsonObject::class.java)
                .getAsJsonArray("items").map {
                    it.asJsonObject.let {
                        val chapterName = it.getAsJsonPrimitive("chapter_name").asString
                        val chapterId = it.getAsJsonPrimitive("chapter_id").asInt.toString()
                        NovelChapter(chapterName, "${bookId}_$chapterId", null)
                    }
                }
        }
    }
    // https://www.ttkan.co/novel/user/page_direct?novel_id=shisiruguiweijunzi-pingceng&page=2
    // https://www.bg3.co/novel/pagea/shisiruguiweijunzi-pingceng_2.html
    contentPageTemplate = "//www.bg3.co/novel/pagea/%s.html"
    content {
        val bookIdAndChapterId = findBookIdWithChapterId(it)
        val (bookId, chapterId) = bookIdAndChapterId.split('_')
        get {
            url = "/novel/user/page_direct"
            data {
                "novel_id" to bookId
                "page" to chapterId
            }
        }
        document {
            items("div.content")
        }
    }
}

    // 用来解析章节api,
    private val gson: Gson = GsonUtils.gson
}

