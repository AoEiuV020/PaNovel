package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext

/**
 * 日本轻小说书源，
 *
 * Created by AoEiuV020 on 2018.09.04-03:46:55.
 */
class Qinxiaoshuo : DslJsoupNovelContext() {init {
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
            items("#body > div.book_list > div.book_item") {
                name("> div.item_right > div.item_name > a")
                // 搜索结果没给作者，
                author = ""
            }
        }
    }
    // https://www.qinxiaoshuo.com/book/%e4%b8%ba%e7%be%8e%e5%a5%bd%e7%9a%84%e4%b8%96%e7%95%8c%e7%8c%ae%e4%b8%8a%e7%a5%9d%e7%a6%8f%ef%bc%81%28%e7%bb%99%e4%ba%88%e8%bf%99%e4%b8%aa%e7%bb%9d%e7%be%8e%e7%9a%84%e4%b8%96%e7%95%8c%e4%bb%a5%e7%a5%9d%e7%a6%8f%ef%bc%81%29
    bookIdRegex = "/book/(.*)"
    detailPageTemplate = "/book/%s"
    detail {
        document {
            novel {
                name("#book_info_right > h1")
                author("#book_info_right > p:nth-child(4) > a")
            }
            image("#book_info > img")
            update("#body > div.branch > div.branch_head > div.head_left > p:nth-child(2)",
                    format = "yyyy-MM-dd HH:mm:ss", block = pickString("更新时间:(.*)"))
            introduction("#book_intro")
        }
    }
    chapters {
        document {
            volumes("#body > div.branch > div[class^='volume']")
            items("> div.chapters > a")
            lastUpdate("#body > div.branch > div.branch_head > div.head_left > p:nth-child(2)",
                    format = "yyyy-MM-dd HH:mm:ss", block = pickString("更新时间:(.*)"))
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
}
}
