package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.base64Decode
import cc.aoeiuv020.base.jar.cipherDecrypt
import cc.aoeiuv020.base.jar.md5
import cc.aoeiuv020.base.jar.sha1
import cc.aoeiuv020.jsonpath.get
import cc.aoeiuv020.jsonpath.jsonPath
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstIntPattern
import org.jsoup.Jsoup
import java.nio.charset.Charset

/**
 * 日本轻小说书源，
 *
 * Created by AoEiuV020 on 2018.09.04-03:46:55.
 */
class Lnovel : DslJsoupNovelContext() {init {
    site {
        name = "lnovel"
        baseUrl = "https://lnovel.cc/"
        logo = "https://lnovel.cc/static/images/logo.png"
    }
    search {
        get {
            // https://lnovel.cc/search/%E4%BB%8E%E9%9B%B6%E5%BC%80%E5%A7%8B.html
            url = "/search/$it.html"
        }
        document {
            items("div.mdl-grid > div") {
                /*
                <a href="/book/1861.html"><h2 class="mdl-card__title-text">Re:从零开始的异世界生活</h2></a>
                 */
                name("> div.yofiction-book-description > a")
                author("> div.yofiction-book-description > div.mdl-card__supporting-text.yofiction-book-info > span")
            }
        }
    }
    // https://lnovel.cc/book/1861.html
    detailPageTemplate = "/book/%s.html"
    detail {
        document {
            novel {
                name("#content  div.yofiction-book_info > div.yofiction-book-description > h1 > em")
                author("#content  div.yofiction-book_info > div.yofiction-book-description > div.mdl-card__supporting-text.author.ellipsis-1 > span")
            }
            image("#content  div.yofiction-book_info > img")
            update("#content  div.yofiction-book_info > div.yofiction-book-description > div.mdl-card__supporting-text.yofiction-book-info.ellipsis-1 > span:nth-child(3)",
                    format = "yyyy-MM-dd", block = pickString("\uD83D\uDD5B (.*)"))
            introduction("#info-panel > div")
        }
    }
    chapters {
        document {
            volumes("#catalog-panel > div > ul") {
                reversed()
            }
            items("> li > a")
            lastUpdate("#content  div.yofiction-book_info > div.yofiction-book-description > div.mdl-card__supporting-text.yofiction-book-info.ellipsis-1 > span:nth-child(3)",
                    format = "yyyy-MM-dd", block = pickString("\uD83D\uDD5B (.*)"))
        }
    }
    // https://lnovel.cc/read/75861.html
    // https://lnovel.cc/content/75861.json
    bookIdWithChapterIdRegex = firstIntPattern
    contentPageTemplate = "/read/%s.html"
    content {
        // 没必要，一个看起来正经的user agent,
        val ua = """Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36"""
        get {
            url = "/content/$it.json"
            header {
                // 没必要，referer只要是这个网站都可以，
                referer = absUrl("/read/%s.html".format(it))
                userAgent = ua
            }
        }
        response {
            val key = " . $ua".md5()
            val iv = (ua + "novel").sha1().run {
                // hex().drop(4).take(32)
                ByteArray(16) {
                    get(it + 2)
                }
            }
            val content = it.jsonPath.get<String>("content")
                    .base64Decode()
                    .let {
                        // 手动ZeroPadding, 补齐16字节，
                        // 好像会错意了，补零是加密前补的，但不要紧，这里应该是必然是16的倍数，
                        if (it.size % 16 != 0) {
                            val newSize = (it.size / 16 + 1) * 16
                            it.copyOf(newSize)
                        } else {
                            it
                        }
                    }
            val plant = content.cipherDecrypt(key, iv, "AES", "CBC", "NoPadding")
                    .toString(Charset.defaultCharset())

            document(Jsoup.parse(plant, request.url().toString())) {
                items("body")
            }
        }
    }
}
}
