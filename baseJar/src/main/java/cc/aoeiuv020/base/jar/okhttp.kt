package cc.aoeiuv020.base.jar

import cc.aoeiuv020.base.jar.ssl.TLSSocketFactory
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.net.MalformedURLException
import java.net.URL
import java.security.KeyStore
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * Created by AoEiuV020 on 2018.06.10-15:56:00.
 */
val baseClientBuilder: OkHttpClient.Builder by lazy {
    OkHttpClient.Builder()
            .sslSocketFactory(TLSSocketFactory(), trustManager)
}

val client: OkHttpClient by lazy {
    baseClientBuilder.build()
}
private val trustManager: X509TrustManager
    get() = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            .apply { init(null as KeyStore?) }
            .trustManagers
            .first {
                it is X509TrustManager
            } as X509TrustManager

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
