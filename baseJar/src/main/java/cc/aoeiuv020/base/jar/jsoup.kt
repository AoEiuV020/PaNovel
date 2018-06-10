package cc.aoeiuv020.base.jar

import okhttp3.Call
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Created by AoEiuV020 on 2018.06.10-15:56:52.
 */

fun jsoupParse(call: Call): Document {
    val response = call.execute()
    return response.body().notNull().use {
        it.byteStream().use { input ->
            Jsoup.parse(input, response.charset(), response.url())
        }
    }
}

fun jsoupConnect(url: String): Document = jsoupParse(get(url))
