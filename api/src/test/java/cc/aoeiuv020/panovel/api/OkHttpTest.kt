package cc.aoeiuv020.panovel.api

import cc.aoeiuv020.base.jar.info
import cc.aoeiuv020.base.jar.notNull
import okhttp3.*
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URLEncoder
import java.nio.charset.Charset

/**
 * Created by AoEiuV020 on 2018.06.01-23:01:40.
 */
class OkHttpTest {
    protected val logger: Logger = LoggerFactory.getLogger(this.javaClass.simpleName)

    @Test
    fun url() {
        val httpUrl = HttpUrl.parse("http://q.w/asdf?z=x")!!
        assertEquals("http://q.w/asdf?z=x", httpUrl.toString())
        val cookie = Cookie.parse(httpUrl, " a =  b")
        assertEquals("a=b; path=/", cookie.toString())
    }

    @Test
    fun redirect() {
        val url = "https://baidu.com"
        val request = Request.Builder().url(url)
                .build()
        val call = OkHttpClient().newCall(request)
        val response = call.execute()

        assertEquals("https://baidu.com", url)
        assertEquals("https://baidu.com/", request.url().toString())
        assertEquals("http://www.baidu.com/", response.request().url().toString())
        assertEquals(false, response.isRedirect)

    }

    @Test
    fun closeTest() {
        val url = "http://www.baidu.com"
        val request = Request.Builder().url(url)
                .build()
        val call = OkHttpClient().newCall(request)
        val response = call.execute()
        val text = response.body()?.use { body ->
            body.byteStream().use { input ->
                input.bufferedReader().readText()
            }
        }
        assertFalse(text.isNullOrBlank())
    }

    @Test
    fun callRequest() {
        val url = "http://www.baidu.com"
        val request = Request.Builder().url(url)
                .build()
        val call = OkHttpClient().newCall(request)
        assertEquals("http://www.baidu.com/", call.request().url().toString())
    }

    @Test
    fun queryTest() {
        val url = "http://s.sfacg.com"
        val httpUrl = HttpUrl.parse(url)!!.newBuilder()
                .addQueryParameter("Key", "都市")
                .addQueryParameter("Key", "都市")
                .addQueryParameter("Key", URLEncoder.encode("都市", "GBK"))
                .addEncodedQueryParameter("Key", URLEncoder.encode("都市", "GBK"))
                .build()

        val requestBody = FormBody.Builder(Charset.forName("GBK"))
                .add("Key", "都市")
                .add("Key", "都市")
                .build()

        val request = Request.Builder().url(httpUrl)
                .post(requestBody)
                .build()

        assertEquals("http://s.sfacg.com/?Key=%E9%83%BD%E5%B8%82&Key=%E9%83%BD%E5%B8%82&Key=%25B6%25BC%25CA%25D0&Key=%B6%BC%CA%D0",
                request.url().toString())
        val buffer = Buffer()
        request.body()?.writeTo(buffer)
        assertEquals("Key=%B6%BC%CA%D0&Key=%B6%BC%CA%D0", buffer.readUtf8())
    }

    @Test
    fun cookie() {
        val client = OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .addInterceptor {
                    val response = it.proceed(it.request())
                    val headers = response.networkResponse()!!.request().headers()
                    val cookie = headers["Cookie"]
                    logger.info { "request cookie $cookie" }
                    response
                }
                .build()
        val request = Request.Builder()
                .url("https://www.piaotian.com/modules/article/search.php?searchtype=articlename&searchkey=%B6%BC%CA%D0&page=1")
                .build()
        client.newCall(request).execute()
        client.newCall(request).execute()

    }

    val cookieJar = object : CookieJar {
        private val cookies = mutableMapOf<String, Cookie>()
        override fun saveFromResponse(url: HttpUrl, cookies: MutableList<Cookie>) {
            logger.info { "save cookies $cookies" }
            this.cookies.putAll(cookies.map { it.name() to it })
        }

        override fun loadForRequest(url: HttpUrl): MutableList<Cookie> {
            return cookies.values.toMutableList().also {
                logger.info { "load cookies $it" }
            }
        }
    }

    @Test
    fun path() {
        val bookId = "1234"
        val chapterId = "5678"
        val url = "https://m.qidian.com/majax/chapter/getChapterInfo?bookId=$bookId&chapterId=$chapterId"
        val httpUrl = HttpUrl.parse(url).notNull()
        assertEquals("/majax/chapter/getChapterInfo", httpUrl.encodedPath())
        assertEquals("bookId=1234&chapterId=5678", httpUrl.query())
    }
}
