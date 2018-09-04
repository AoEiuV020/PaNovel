package cc.aoeiuv020.base.jar

import cc.aoeiuv020.base.jar.ssl.TLSSocketFactory
import okhttp3.*
import java.net.MalformedURLException
import java.net.URL
import java.security.KeyStore
import java.security.cert.X509Certificate
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * Created by AoEiuV020 on 2018.06.10-15:56:00.
 */
@Suppress("unused")
val trustAllManager: X509TrustManager = object : X509TrustManager {
    @SuppressWarnings("TrustAllX509TrustManager")
    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
    }

    @SuppressWarnings("TrustAllX509TrustManager")
    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return emptyArray()
    }
}

val trustManager: X509TrustManager = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        .apply { init(null as KeyStore?) }
        .trustManagers
        .first {
            it is X509TrustManager
        } as X509TrustManager

// https://github.com/square/okhttp/issues/4053
// Add legacy cipher suite for Android 4
private val cipherSuites = ConnectionSpec.MODERN_TLS.cipherSuites()
        .orEmpty()
        .toMutableList().apply {
            if (!contains(CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA)) {
                /*
                javax.net.ssl.SSLProtocolException: SSL handshake aborted: ssl=0xb88ec0b0: Failure in SSL library, usually a protocol error
            error:14077410:SSL routines:SSL23_GET_SERVER_HELLO:sslv3 alert handshake failure (external/openssl/ssl/s23_clnt.c:741 0x98947990:0x00000000)
                 */
                add(CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA)
            }
        }

private val spec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
        .cipherSuites(*cipherSuites.toTypedArray())
        .build()

/**
 * 私有，外部不要用，不要改了这个builder, 需要时baseClient.newBuilder(),
 */
private val baseClientBuilder: OkHttpClient.Builder by lazy {
    OkHttpClient.Builder()
            // cleartext要明确指定，
            .connectionSpecs(listOf(spec, ConnectionSpec.CLEARTEXT))
            .sslSocketFactory((TLSSocketFactory(trustManager)), trustManager)
}

val baseClient: OkHttpClient by lazy {
    baseClientBuilder.build()
}

fun get(url: String): Call {
    val request = Request.Builder()
            .url(url)
            .build()
    return baseClient.newCall(request)
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
