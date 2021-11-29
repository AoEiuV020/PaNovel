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
            val head = mutableListOf<Post>()
            var title = ""
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
                    title = root.selectFirst("div.panel-heading > h1").text().removePrefix("起点新书上架首订")
                } catch (e: Exception) {
                    error({ "解析标题失败" }, e)
                }
                try {
                    root.select("div.panel-heading > a").forEach {
                        head.add(Post(it.text(), it.absHref()))
                    }
                } catch (e: Exception) {
                    error({ "解析头部链接失败" }, e)
                }
                try {
                    root.select("#shouding_table > tbody > tr")
                        ?.takeIf { it.isNotEmpty() }.notNull()
                } catch (e: Exception) {
                    throw IllegalStateException("解析小说列表失败", e)
                }.mapNotNull { tr ->
                    val realOrder: Map<String, Int> = listOf(
                        "order",
                        "name", "type", "author",
                        "level", "fans", "collection",
                        "firstOrder", "ratio", "words",
                        "dateAdded"
                    ).mapIndexed { index, s -> s to index }.toMap()
                    val children = tr.children()
                    if (children.size == 1 && children.first().text().contains("无新书上架")) {
                        return@mapNotNull null
                    }
                    val (name, url) = try {
                        children[realOrder["name"].notNull("name")].child(0).run {
                            Pair(text(), absHref())
                        }
                    } catch (e: Exception) {
                        throw IllegalStateException("解析书名失败", e)
                    }
                    val author = try {
                        children[realOrder["author"].notNull("author")].text()
                    } catch (e: Exception) {
                        throw IllegalStateException("解析作者名失败", e)
                    }
                    val itemOrder = listOf(
                        "level",
                        "type",
                        "words",
                        "collection",
                        "firstOrder",
                        "ratio",
                        "dateAdded"
                    )
                    val info = itemOrder.map {
                        val index = realOrder[it]
                        try {
                            children[index.notNull(it)].text() ?: ""
                        } catch (e: Exception) {
                            error({ "解析信息失败: $index" }, e)
                            ""
                        }
                    }
                    ret.add(
                        Item(
                            url, name, author,
                            info[0], info[1], info[2],
                            info[3], info[4], info[5],
                            info[6]
                        )
                    )
                }
            }
            saveCache(ret, head, title)
            uiThread {
                showResult(ret, head, title)
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
            val head = root.read<List<Post>>(keyCache + "head") ?: listOf()
            val title = root.read<String>(keyCache + "title") ?: ""
            uiThread {
                showResult(data, head, title)
            }
        }
    }

    @WorkerThread
    private fun saveCache(data: List<Item>, head: List<Post>, title: String) {
        root.write(keyCache, data)
        root.write(keyCache + "head", head)
        root.write(keyCache + "title", title)
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

    private fun showResult(data: List<Item>, head: List<Post>, title: String) {
        view?.showResult(data, head, title)
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