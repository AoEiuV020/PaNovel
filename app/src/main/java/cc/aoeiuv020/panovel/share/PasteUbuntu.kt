package cc.aoeiuv020.panovel.share

import org.jsoup.Connection
import org.jsoup.Jsoup

/**
 * 网上贴文本，免费还无限制，
 *
 * Created by AoEiuV020 on 2018.03.07-19:16:41.
 */
internal class PasteUbuntu {
    companion object {
        val homePage = "https://paste.ubuntu.com/"
    }

    fun check(url: String): Boolean {
        return url.startsWith(homePage)
    }

    /**
     * 上传文本，返回生成页面的地址，
     */
    fun upload(data: PasteUbuntuData): String {
        val response = Jsoup.connect(homePage)
                .followRedirects(false)
                .maxBodySize(0)
                .method(Connection.Method.POST)
                .data(data.toMap())
                .execute()
        return response.header("Location")
    }

    fun download(url: String): String {
        val root = Jsoup.connect(url)
                .maxBodySize(0)
                .get()
        return root.select("#contentColumn > div > div > div > table > tbody > tr > td.code > div > pre").first().text()
    }

    class PasteUbuntuData(
            private var content: String,
            private var poster: String = "PaNovel",
            private var syntax: String = "text",
            private var expiration: Expiration = Expiration.DAY
    ) {
        fun toMap(): Map<String, String> {
            return mapOf("content" to content,
                    "poster" to poster,
                    "syntax" to syntax,
                    "expiration" to expiration.value)
        }
    }

}
