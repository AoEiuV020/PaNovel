package cc.aoeiuv020.panovel.share

import cc.aoeiuv020.base.jar.jsoupConnect
import cc.aoeiuv020.okhttp.OkHttpUtils
import cc.aoeiuv020.okhttp.url
import cc.aoeiuv020.panovel.util.notNullOrReport
import okhttp3.FormBody
import okhttp3.Request
import java.net.URL

/**
 * 网上贴文本，免费还无限制，
 *
 * Created by AoEiuV020 on 2018.03.07-19:16:41.
 */
internal class PasteUbuntu {
    companion object {
        const val homePage = "https://paste.ubuntu.com/"
    }

    fun check(url: String): Boolean {
        return url.startsWith(homePage)
    }

    /**
     * 上传文本，返回生成页面的地址，
     */
    fun upload(data: PasteUbuntuData): String {
        val form = FormBody.Builder()
                .apply {
                    data.toMap().forEach { (name, value) ->
                        add(name, value)
                    }
                }
                .build()
        val request = Request.Builder()
                .url(homePage)
                .post(form)
                .build()
        return OkHttpUtils.client.newBuilder().followRedirects(false)
                .build()
                .newCall(request)
                .execute().let { response ->
                    val l = response.header("Location")
                            .notNullOrReport()
                    URL(URL(response.url()), l).toString()
                }
    }

    fun download(url: String): String {
        val root = jsoupConnect(url)
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
