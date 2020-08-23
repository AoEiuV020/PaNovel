package cc.aoeiuv020.panovel.api.base

import cc.aoeiuv020.anull.notNull
import cc.aoeiuv020.log.debug
import cc.aoeiuv020.log.error
import cc.aoeiuv020.log.info
import cc.aoeiuv020.okhttp.OkHttpUtils
import cc.aoeiuv020.okhttp.sslAllowAll
import cc.aoeiuv020.panovel.api.LoggerInputStream
import cc.aoeiuv020.panovel.api.NovelContext
import okhttp3.*
import okio.Buffer
import java.io.InputStream

/**
 * Created by AoEiuV020 on 2018.06.01-20:43:49.
 */
abstract class OkHttpNovelContext : NovelContext() {
    protected val defaultHeaders: MutableMap<String, String> by lazy {
        mutableMapOf(
                "Referer" to site.baseUrl,
                "User-Agent" to "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36"
        )
    }

    // 子类可以继承自己的clientBuilder, 然后不能影响得到client, 要用lazy,
    protected open val client: OkHttpClient by lazy { clientBuilder.build() }


    // 子类可以继承，只在第一次使用client时使用一次，
    protected open val clientBuilder: OkHttpClient.Builder
        // 每次都生成新的builder，以免一个网站加的设置影响到其他网站，
        get() = OkHttpUtils.client.newBuilder()
/*
                .proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress("localhost", 8080)))
*/
                .sslAllowAll()
                .addInterceptor(LogInterceptor())
                .addInterceptor(HeaderInterceptor())
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
                    cookieFilter(url, it).also {
                        logger.debug { "after filter cookies $it" }
                    }
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
    protected fun <T> Response.inputStream(
            listener: ((Long, Long) -> Unit)? = null,
            block: (InputStream) -> T
    ): T = body().notNull().use {
        val maxSize = it.contentLength()
        LoggerInputStream(it.byteStream(), maxSize, listener).use(block)
    }

    protected fun Response.charset(): String? = body()?.contentType()?.charset()?.name()

    protected fun Response.url(): String = this.request().url().toString()


    protected fun connect(url: String, post: Boolean = false): Call {
        val request = Request.Builder()
                .url(url)
                .apply {
                    defaultHeaders.forEach { (key, value) ->
                        header(key, value)
                    }
                    if (post) {
                        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                        post(RequestBody.create(null, ""))
                    }
                }
                .build()
        return client.newCall(request)
    }

    protected fun response(call: Call): Response {
        val response = call.execute()
        if (!check(response.url())) {
            // 可能网络需要登录之类的，会跳到不认识的地址，
            // 可能误伤，比如网站自己换域名，
            // TODO: 日志要支持发行版上传bugly,
            // 目前这样如果后面解析失败，上传失败日志时会带上这条日志，
            logger.error { "网络被重定向，<${call.request().url()}> -> <${response.url()}>" }
//            throw IOException("网络被重定向，检查网络是否可用，")
        }
        return response
    }

    protected fun responseBody(call: Call): ResponseBody {
        // 不可能为空，execute得到的response一定有body,
        return response(call).body().notNull()
    }

    private inner class LogInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            logger.info { "connect ${request.url()}" }
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

    private inner class HeaderInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            var request = chain.request()
            if (headers.isNotEmpty()) {
                val requestBuilder = request.newBuilder()
                headers.map { (name, value) ->
                    requestBuilder.header(name, value)
                }
                request = requestBuilder.build()
            }
            val response = chain.proceed(request)
            return response
        }
    }

}