@file:Suppress("DEPRECATION")


package cc.aoeiuv020.panovel.find.shuju.post

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import cc.aoeiuv020.anull.notNull
import cc.aoeiuv020.base.jar.absHref
import cc.aoeiuv020.irondb.Database
import cc.aoeiuv020.irondb.Iron
import cc.aoeiuv020.irondb.read
import cc.aoeiuv020.irondb.write
import cc.aoeiuv020.okhttp.OkHttpUtils
import cc.aoeiuv020.okhttp.string
import cc.aoeiuv020.panovel.Presenter
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.data.CacheManager
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.settings.LocationSettings
import org.jetbrains.anko.*
import org.jsoup.Jsoup
import java.io.File
import java.text.SimpleDateFormat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

@Suppress("SameParameterValue")
class QidianshujuPostPresenter : Presenter<QidianshujuPostActivity>(), AnkoLogger {
    private var requesting = false
    private val baseUrl = "http://pujie.qidianshuju.com/uln/1002.html"
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()
    private var currentWorker: Future<Unit>? = null
    private val maxRetry = 100
    private val keyCache = "0"

    @SuppressLint("SimpleDateFormat")
    private val sdfPost = SimpleDateFormat("yyyy-MM-dd HH:mm")
    private lateinit var root: Database

    fun start(ctx: Context) {
        debug { "start," }
        root = initCacheLocation(ctx)
        loadCache()
    }

    fun refresh() {
        debug { "refresh," }
        if (requesting) {
            stop()
        }
        request()
    }

    fun stop() {
        val future = currentWorker ?: return
        requesting = false
        future.cancel(true)
    }

    @MainThread
    private fun request() {
        requesting = true
        currentWorker = view?.doAsync({ t ->
            if (t is InterruptedException) {
                info { "stopped" }
                return@doAsync
            }
            requesting = false
            view?.runOnUiThread {
                showFailed("获取文章列表失败", t)
            }
        }, executorService) {
            val ret = mutableListOf<Post>()
            var code = -1
            var retry = 0
            while (requesting && code != 200 && ret.isEmpty() && retry < maxRetry) {
                retry++
                val response = OkHttpUtils.get(baseUrl).execute()
                code = response.code()
                if (code != 200) {
                    uiThread {
                        showProgress(retry)
                    }
                    Thread.sleep(200)
                    continue
                }
                OkHttpUtils.get(baseUrl).string()
                val html = response.body().notNull().string()
                val root = Jsoup.parse(html, baseUrl)
                try {
                    root.select("#unotehistory > tbody > tr:not(:nth-child(1))")
                        ?.takeIf { it.isNotEmpty() }.notNull()
                } catch (e: Exception) {
                    throw IllegalStateException("解析文章列表失败", e)
                }.map { tr ->
                    val (title, url) = try {
                        tr.selectFirst("> td:nth-child(1) > a").run {
                            Pair(text(), absHref())
                        }
                    } catch (e: Exception) {
                        throw IllegalStateException("解析标题失败", e)
                    }
                    val num = try {
                        tr.selectFirst("> td:nth-child(3)").text()
                    } catch (e: Exception) {
                        error({ "解析查看数失败" }, e)
                        null
                    } ?: ""
                    val date = try {
                        tr.selectFirst("> td:nth-child(4)").text().let { s ->
                            sdfPost.parse(s)
                        }
                    } catch (e: Exception) {
                        error({ "解析查看数失败" }, e)
                        null
                    }
                    ret.add(Post(url, title, num, date))
                }
            }
            saveCache(ret)
            uiThread {
                showResult(ret)
            }
        }
    }

    @MainThread
    private fun loadCache() {
        currentWorker = view?.doAsync({ t ->
            if (t is InterruptedException) {
                info { "stopped" }
                return@doAsync
            }
            view?.runOnUiThread {
                showFailed("加载文章列表缓存失败", t)
            }
        }, executorService) {
            val data = root.read<List<Post>>(keyCache) ?: run {
                uiThread {
                    request()
                }
                return@doAsync
            }
            uiThread {
                showResult(data)
            }
        }
    }

    @WorkerThread
    private fun saveCache(data: MutableList<Post>) {
        root.write(keyCache, data)
    }

    private fun initCacheLocation(ctx: Context): Database = try {
        Iron.db(File(LocationSettings.cacheLocation))
    } catch (e: Exception) {
        Reporter.post("初始化缓存目录<${LocationSettings.cacheLocation}>失败，", e)
        ctx.toast(
            ctx.getString(
                R.string.tip_init_cache_failed_place_holder,
                LocationSettings.cacheLocation
            )
        )
        // 失败一次就改成默认的，以免反复失败，
        LocationSettings.cacheLocation = ctx.cacheDir.resolve(CacheManager.NAME_FOLDER).absolutePath
        Iron.db(File(LocationSettings.cacheLocation))
    }.sub("qidianshuju")

    private fun showResult(data: List<Post>) {
        view?.showResult(data)
    }

    private fun showProgress(retry: Int) {
        view?.showProgress(retry, maxRetry)
    }

    private fun showFailed(message: String, t: Throwable) {
        error(message, t)
        view?.showError(message, t)
    }

    fun browse() {
        view?.innerBrowse(baseUrl)
    }

}