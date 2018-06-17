package cc.aoeiuv020.base.jar

import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.net.MalformedURLException
import java.net.URL

/**
 * Created by AoEiuV020 on 2018.06.10-15:56:00.
 */
val client: OkHttpClient by lazy {
    OkHttpClient.Builder()
            .build()
}

fun get(url: String): Call {
    val request = Request.Builder()
            .url(url)
            .build()
    return client.newCall(request)
}

fun Call.string(): String = this.execute().body().notNull().use { it.string() }

fun Response.charset(): String? = body()?.contentType()?.charset()?.name()
fun Response.url(): String = this.request().url().toString()
/**
 * 地址仅路径，斜杆/开头，
 */
fun path(url: String): String = try {
    URL(url).path
} catch (e: MalformedURLException) {
    url
}
