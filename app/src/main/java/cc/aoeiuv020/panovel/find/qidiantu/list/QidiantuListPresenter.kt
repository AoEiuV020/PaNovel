@file:Suppress("DEPRECATION")


package cc.aoeiuv020.panovel.find.qidiantu.list

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import cc.aoeiuv020.anull.notNull
import cc.aoeiuv020.base.jar.absHref
import cc.aoeiuv020.encrypt.hex
import cc.aoeiuv020.irondb.Database
import cc.aoeiuv020.irondb.Iron
import cc.aoeiuv020.irondb.read
import cc.aoeiuv020.irondb.write
import cc.aoeiuv020.okhttp.OkHttpUtils
import cc.aoeiuv020.okhttp.string
import cc.aoeiuv020.panovel.Presenter
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.data.CacheManager
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.settings.LocationSettings
import org.jetbrains.anko.*
import org.jsoup.Jsoup
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

@Suppress("SameParameterValue")
class QidiantuListPresenter : Presenter<QidiantuListActivity>(), AnkoLogger {
    private var requesting = false
    private lateinit var baseUrl: String
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()
    private var currentWorker: Future<Unit>? = null
    private val maxRetry = 5
    private lateinit var keyCache: String

    @SuppressLint("SimpleDateFormat")
    private lateinit var root: Database

    fun start(ctx: Context, url: String) {
        debug { "start," }
        this.baseUrl = url
        keyCache = url.toByteArray().hex()
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
                showFailed("获取小说列表失败", t)
            }
        }, executorService) {
            val ret = mutableListOf<Item>()
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
                    root.select("#shouding_table > tbody > tr")
                        ?.takeIf { it.isNotEmpty() }.notNull()
                } catch (e: Exception) {
                    throw IllegalStateException("解析小说列表失败", e)
                }.map { tr ->
                    val children = tr.children()
                    val (title, url) = try {
                        children[1].child(0).run {
                            Pair(text(), absHref())
                        }
                    } catch (e: Exception) {
                        throw IllegalStateException("解析书名失败", e)
                    }
                    val author = try {
                        children[5].text()
                    } catch (e: Exception) {
                        throw IllegalStateException("解析作者名失败", e)
                    }
                    val infoIndexList = listOf(2,3,4,6,7,8,9)
                    val info = infoIndexList.map { index ->
                        try {
                            children[index].text()
                        } catch (e: Exception) {
                            error({ "解析信息失败: $index" }, e)
                            ""
                        }
                    }
                    ret.add(
                        Item(
                            url, title, author,
                            info[5], info[3], info[4],
                            info[2], info[0], info[1],
                            info[6]
                        )
                    )
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
                showFailed("加载小说列表缓存失败", t)
            }
        }, executorService) {
            val data = root.read<List<Item>>(keyCache) ?: run {
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
    private fun saveCache(data: MutableList<Item>) {
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
    }.sub("Qidiantu")

    private fun showResult(data: List<Item>) {
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

    fun open(item: Item, bookId: String) {
        debug { "open <$item>," }
        view?.doAsync({ e ->
            val message = "打开地址<$item>失败，"
            Reporter.post(message, e)
            error(message, e)
            view?.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            val novelManager = DataManager.query("起点中文", item.author, item.name, bookId)
            val novel = novelManager.novel
            uiThread {
                view?.openNovelDetail(novel)
            }
        }
    }
}