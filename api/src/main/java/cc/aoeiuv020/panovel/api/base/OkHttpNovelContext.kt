package cc.aoeiuv020.panovel.api.base

import cc.aoeiuv020.base.jar.debug
import cc.aoeiuv020.base.jar.notNull
import cc.aoeiuv020.base.jar.ssl.TLSSocketFactory
import cc.aoeiuv020.panovel.api.NovelContext
import okhttp3.*
import okio.Buffer
import java.io.IOException
import java.io.InputStream
import java.security.KeyStore
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * Created by AoEiuV020 on 2018.06.01-20:43:49.
 */
abstract class OkHttpNovelContext : NovelContext() {
    // 子类可以继承自己的clientBuilder, 然后不能影响得到client, 要用lazy,
    protected open val client: OkHttpClient by lazy { clientBuilder.build() }

    @Suppress("CAST_NEVER_SUCCEEDS")
    private val trustManager: X509TrustManager
        get() = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                .apply { init(null as? KeyStore) }
                .trustManagers
                .first {
                    it is X509TrustManager
                } as X509TrustManager

    // 子类可以继承，只在第一次使用client时使用一次，
    protected open val clientBuilder: OkHttpClient.Builder
        get() = OkHttpClient.Builder()
                .addInterceptor(LogInterceptor())
                // 没具体测试，低版本安卓可能https握手失败，
                // 是某个tls协议没有启用导致胡，
                // 这个工厂类启用了所有支持的ssl,
                .sslSocketFactory(TLSSocketFactory(), trustManager)
                .cookieJar(cookieJar)
                // 一个网站20M缓存，
                // 还不清楚缓存会被具体用在什么地方，
                .cache(mCacheDir?.let { Cache(it, 20 * 1000 * 1000) })

    // 只在第一次使用client时使用一次，传入client中，
    private val cookieJar
        get() = object : CookieJar {
            override fun saveFromResponse(url: HttpUrl, cookies: MutableList<Cookie>) {
                logger.debug { "save cookies $cookies" }
                putCookies(cookies)
            }

            override fun loadForRequest(url: HttpUrl): MutableList<Cookie> {
                return cookies.values.toMutableList().let {
                    logger.debug { "load cookies $it" }
                    cookieFilter(url, it)
                }
            }
        }

    /**
     * 有的网站有的页面不能传指定cookie，// 或者必须拥有指定cookie,
     */
    protected open fun cookieFilter(url: HttpUrl, cookies: MutableList<Cookie>): MutableList<Cookie> {
        // 默认不过滤，
        return cookies
    }

    protected val defaultCharset: String = "UTF-8"


    protected fun Response.requestHeaders(): Headers = networkResponse().notNull().request().headers()

    /**
     * okhttp有时候需要传入httpUrl，但是其实具体是什么地址都可以，
     */
    @Suppress("MemberVisibilityCanBePrivate")
    protected val baseHttpUrl: HttpUrl by lazy { HttpUrl.parse(site.baseUrl).notNull() }

    protected fun Headers.requestCookies(): List<Cookie> = get("Cookie")?.split(";")?.mapNotNull {
        // 只有键值对, 不管传入什么地址，都能解析，
        Cookie.parse(baseHttpUrl, it)
    } ?: listOf()

    protected fun Headers.responseCookies(): List<Cookie> = Cookie.parseAll(baseHttpUrl, this)

    /**
     * 取出指定name的cookie,
     */
    protected operator fun List<Cookie>.get(name: String): String? =
            firstOrNull { it.name() == name }?.value()

    // close基本上有重复，但是可以重复关闭，
    protected inline fun <reified T> Response.inputStream(block: (InputStream) -> T): T =
            body().notNull().use { it.byteStream().use(block) }

    protected fun Response.charset(): String? = body()?.contentType()?.charset()?.name()

    protected fun Response.url(): String = this.request().url().toString()


    protected fun connect(url: String): Call {
        val request = Request.Builder()
                .url(url)
                .build()
        return client.newCall(request)
    }

    protected fun response(call: Call): Response {
        val response = call.execute()
        if (!check(response.request().url().toString())) {
            // 可能网络需要登录之类的，会跳到不认识的地址，
            // 可能误伤，比如网站自己换域名，
            throw IOException("网络被重定向，检查网络是否可用，")
        }
        return response
    }

    protected fun responseBody(call: Call): ResponseBody {
        val response = call.execute()
        if (!check(response.request().url().toString())) {
            // 可能网络需要登录之类的，会跳到不认识的地址，
            // 可能误伤，比如网站自己换域名，
            throw IOException("网络被重定向，检查网络是否可用，")
        }
        // 不可能为空，execute得到的response一定有body,
        return response.body().notNull()
    }

    private inner class LogInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            logger.debug { "connect ${request.url()}" }
            logger.debug {
                val buffer = Buffer()
                request.body()?.writeTo(buffer)
                "body ${buffer.readUtf8()}"
            }
            val response = chain.proceed(request)
            logger.debug { "response ${response.request().url()}" }
            // 应该没有不是网络请求的情况，但是不了解okhttp的缓存，但还是不要在这里用可能抛异常的拓展方法requestHeaders，
            logger.debug { "request.headers ${response.networkResponse()?.request()?.headers()}" }
            logger.debug { "response.headers ${response.headers()}" }
            return response
        }
    }

}