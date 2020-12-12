package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.jsonpath.get
import cc.aoeiuv020.jsonpath.jsonPath
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.regex.pick
import com.google.gson.annotations.SerializedName

/**
 * 日本轻小说书源，
 *
 * Created by AoEiuV020 on 2018.09.04-03:46:55.
 */
class Qinxiaoshuo : DslJsoupNovelContext() {init {
    login = true
    site {
        name = "亲小说"
        baseUrl = "https://www.qinxiaoshuo.com"
        logo = "http://static.qinxiaoshuo.com:4000/static/logo-pc.png"
    }
    search {
        get {
            // https://www.qinxiaoshuo.com/search/?keyword=%E4%B8%BA%E7%BE%8E%E5%A5%BD
            url = "/search/"
            data {
                "keyword" to it
            }
        }
        document {
            items("div.book > div.book_info > div.items") {
                name("> h3 > a")
                author("> div:nth-child(2)", block = pickString("作者: (.*)"))
            }
        }
    }
    // https://www.qinxiaoshuo.com/book/%e4%b8%ba%e7%be%8e%e5%a5%bd%e7%9a%84%e4%b8%96%e7%95%8c%e7%8c%ae%e4%b8%8a%e7%a5%9d%e7%a6%8f%ef%bc%81%28%e7%bb%99%e4%ba%88%e8%bf%99%e4%b8%aa%e7%bb%9d%e7%be%8e%e7%9a%84%e4%b8%96%e7%95%8c%e4%bb%a5%e7%a5%9d%e7%a6%8f%ef%bc%81%29
    bookIdRegex = "/book/(.*)"
    detailPageTemplate = "/book/%s"
    detail {
        document {
            novel {
                name("div.right > h1")
                author("div.right > div.info_item:nth-child(2) > div:nth-child(1) > a")
            }
            image("div.show_info > img")
            update("div.right > div.info_item:nth-child(4) > div:nth-child(1)",
                    format = "yyyy-MM-dd HH:mm", block = pickString("更新时间：(.*)"))
            introduction("textarea.intro")
        }
    }
    // https://www.qinxiaoshuo.com/api/user/book/get/1609
    chapters {
        document {
            val id = root.select("img#background_cover").attr("src").pick("/(\\d*).jpg").first()
            val url = "https://www.qinxiaoshuo.com/api/user/book/get/$id"
            val jsonPath = responseBody(connect(url, true)).string().jsonPath
            novelChapterList = jsonPath.get<List<Chapter>>("$.Volumes[*].Chapters[*]").map {
                NovelChapter(it.chapterName, "0/" + id + "/" + it.chapterId)
            }
            lastUpdate("div.right > div.info_item:nth-child(4) > div:nth-child(1)",
                    format = "yyyy-MM-dd HH:mm", block = pickString("更新时间：(.*)"))
        }
    }
    // https://www.qinxiaoshuo.com/read/0/1609/5d77d1cb56fec85e5b10044c.html
    bookIdWithChapterIdRegex = "/read/(.*).html"
    contentPageTemplate = "/read/%s.html"
    content {
        document {
            items("#chapter_content")
        }.toMutableList().dropLastWhile { it == "本章已完，搜索\"亲小说网\"看最新轻小说" }
    }
    cookieFilter {
        // J0FCs41M
        if (!contains("token")) {
            put("token=5fd4c4ea9999497cb43c12d6:1:1607779562:54f84b2213c80c4d99b662eb7a294030")
        }
    }
}

    data class Chapter(
            @SerializedName("Chapter_id")
            val chapterId: String,
            @SerializedName("Chapter_name")
            val chapterName: String,
            @SerializedName("Next_chapter_id")
            val nextChapterId: String,
            @SerializedName("Pre_chapter_id")
            val preChapterId: String,
            @SerializedName("Pv")
            val pv: Int,
            @SerializedName("Translator_name")
            val translatorName: String
    )
}
