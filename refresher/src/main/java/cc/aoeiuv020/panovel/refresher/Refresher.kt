package cc.aoeiuv020.panovel.refresher

import cc.aoeiuv020.gson.toBean
import cc.aoeiuv020.jsonpath.get
import cc.aoeiuv020.jsonpath.jsonPath
import cc.aoeiuv020.log.debug
import cc.aoeiuv020.log.error
import cc.aoeiuv020.log.info
import cc.aoeiuv020.panovel.api.getNovelContextByName
import cc.aoeiuv020.panovel.server.ServerAddress
import cc.aoeiuv020.panovel.server.dal.model.autogen.Novel
import cc.aoeiuv020.panovel.server.service.NovelService
import cc.aoeiuv020.panovel.server.service.impl.NovelServiceImpl
import cc.aoeiuv020.panovel.share.PasteUbuntu
import com.google.gson.JsonObject
import org.slf4j.LoggerFactory
import java.net.URL
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class Refresher(
        private val config: Config
) {
    private val logger = LoggerFactory.getLogger(Refresher::class.java.simpleName)
    private lateinit var service: NovelService
    private var isRunning = false
    private val vipNovelList = mutableSetOf<Novel>()
    fun start(address: ServerAddress, bookshelfList: MutableSet<String>) {
        logger.info {
            config
        }
        logger.info {
            "bookshelfList: $bookshelfList"
        }
        getBookshelf(bookshelfList, config.requireBookshelf)
        isRunning = true
        service = NovelServiceImpl(address)
        val apiUrl = service.config().apiUrl
        if (apiUrl != null) {
            service = NovelServiceImpl(ServerAddress.new(apiUrl))
        }
        logger.info {
            "server: ${service.host}"
        }
        var lastTime = 0L
        var lastCount = 0
        while (isRunning) {
            try {
                // 当前时间，
                val currentTime = System.currentTimeMillis()
                // 本轮耗时，
                val roundTime = currentTime - lastTime
                if (roundTime < TimeUnit.SECONDS.toMillis(1)) {
                    // 无论如何，一轮耗时小于一秒的话，就休息一秒，
                    TimeUnit.SECONDS.sleep(1)
                }
                // 更新记录的时间，
                lastTime = currentTime
                val targetCount = (lastCount * (config.targetTime.toFloat() / roundTime)).toInt().let {
                    if (it <= 0 || it >= config.maxSize) {
                        config.maxSize
                    } else {
                        it
                    }
                }
                logger.info {
                    "roundTime: $roundTime, targetCount: $targetCount, lastCount: $lastCount"
                }
                lastCount = 0
                vipNovelList.also {
                    lastCount += it.size
                }.forEach { novel ->
                    refresh(novel)
                }
                service.needRefreshNovelList(targetCount)
                        .also {
                            if (it.isEmpty()) {
                                throw IllegalStateException("没有需要刷新的了，")
                            }
                        }
                        .also { lastCount += it.size }
                        .forEach { novel ->
                            refresh(novel)
                        }
            } catch (e: Exception) {
                logger.error(e) {
                    "请求需要刷新的小说列表失败，"
                }
                executor.shutdown()
                isRunning = false
                // 有时候出现抛异常却没有停止的情况，原因不明，直接自杀，
                System.exit(1)
            }
        }
    }

    private fun getBookshelf(bookshelfList: MutableSet<String>, requireBookshelf: Boolean) {
        val paste = PasteUbuntu()
        bookshelfList.forEach { url ->
            logger.debug {
                "正在获取书架 $url"
            }
            try {
                val text = if (url.startsWith("file://")) {
                    URL(url).readText()
                } else {
                    if (!paste.check(url)) {
                        return@forEach
                    }
                    paste.download(url)
                }
                val bookListJson = text.toBean<JsonObject>()
                val version = bookListJson.get("version")?.asJsonPrimitive?.asInt
                val bookListBean: BookListBean = when (version) {
                    3 -> {
                        bookListJson.jsonPath.get()
                    }
                    2 -> {
                        bookListJson.jsonPath.get<BookListBean2>().let {
                            BookListBean(it.name, it.list, it.version, UUID.randomUUID().toString())
                        }
                    }
                    1 -> {
                        // 旧版version为null,
                        val bookListBean1: BookListBean1 = bookListJson.jsonPath.get()
                        BookListBean(bookListBean1.name, bookListBean1.list.map {
                            // 旧版的extra为完整地址，直接拿来，就算写进数据库了，刷新详情页后也会被新版的bookId覆盖，
                            NovelMinimal(site = it.site, author = it.author, name = it.name, detail = it.requester.extra)
                        }, 3, UUID.randomUUID().toString())
                    }
                    else -> throw IllegalStateException("APP版本太低")
                }
                bookListBean.list.forEach {
                    logger.info {
                        "获取到书架小说 $it"
                    }
                    val novel = Novel().apply {
                        site = it.site
                        author = it.author
                        name = it.name
                        detail = it.detail
                        chaptersCount = 0
                    }
                    vipNovelList.add(novel)
                }
            } catch (e: Exception) {
                logger.error(e) {
                    "获取书架失败 $url"
                }
                if (requireBookshelf) {
                    throw e
                }
            }
        }
    }

    private val executor: ThreadPoolExecutor =
            ThreadPoolExecutor(config.threads, config.threads, 0L, TimeUnit.MILLISECONDS, LinkedBlockingQueue(1)).apply {
                // 线程满了就休息100ms,
                setRejectedExecutionHandler { runnable, threadPoolExecutor ->
                    TimeUnit.MILLISECONDS.sleep(100)
                    threadPoolExecutor.submit(runnable)
                }
            }

    private fun refresh(novel: Novel) {
        executor.submit {
            refreshActual(novel)
        }
    }

    private fun refreshActual(novel: Novel) {
        if (System.currentTimeMillis() - (novel.checkUpdateTime?.time ?: 0) < config.minTime) {
            logger.info {
                "skip <${novel.run { "$site.$author.$name.$detail" }}>"
            }
            // 避免频繁刷新，
            return
        }
        logger.info {
            "refresh <${novel.run { "$site.$author.$name.$detail" }}>"
        }
        if (config.disableSites.contains(novel.site)) {
            // 有些网站可以选择跳过，可能连不上之类的，超时一直等就不爽了，
            logger.info { "skip <${novel.run { "$site.$author.$name.$detail" }}>" }
            return
        }
        try {
            val context = getNovelContextByName(novel.site)
            if (context.hide || !context.enabled) {
                logger.info { "skip <${novel.run { "$site.$author.$name.$detail" }}>" }
                return
            }
            val chapters = context.getNovelChaptersAsc(novel.detail)
            val hasUpdate = chapters.size > novel.chaptersCount
            novel.chaptersCount = chapters.size
            novel.checkUpdateTime = Date()
            if (hasUpdate) {
                novel.receiveUpdateTime = novel.checkUpdateTime
                service.uploadUpdate(novel)
            } else {
                service.touch(novel)
            }
        } catch (e: Exception) {
            logger.error(e) {
                "刷新失败，<${novel.run { "$site.$author.$name.$detail" }}>"
            }
        }
    }

    @Suppress("unused")
    fun stop() {
        isRunning = false
    }
}
