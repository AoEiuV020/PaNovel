package cc.aoeiuv020.panovel.refresher

import cc.aoeiuv020.base.jar.*
import cc.aoeiuv020.panovel.api.getNovelContextByName
import cc.aoeiuv020.panovel.server.ServerAddress
import cc.aoeiuv020.panovel.server.dal.model.autogen.Novel
import cc.aoeiuv020.panovel.server.service.NovelService
import cc.aoeiuv020.panovel.server.service.impl.NovelServiceImpl
import cc.aoeiuv020.panovel.share.PasteUbuntu
import com.google.gson.JsonObject
import org.slf4j.LoggerFactory
import java.util.*
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
            "start address: ${address.data}"
        }
        logger.info {
            config
        }
        logger.info {
            "bookshelfList: $bookshelfList"
        }
        getBookshelf(bookshelfList, config.requireBookshelf)
        isRunning = true
        service = NovelServiceImpl(address)
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
                isRunning = false
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
                if (!paste.check(url)) {
                    return@forEach
                }
                val text = paste.download(url)
                val bookListJson = text.toBean<JsonObject>()
                val version = bookListJson.get("version")?.asJsonPrimitive?.asInt
                val bookListBean: BookListBean = when (version) {
                    2 -> {
                        bookListJson.jsonPath.get()
                    }
                    else -> {
                        // 旧版version为null,
                        val oldBookListBean: OldBookListBean = bookListJson.jsonPath.get()
                        BookListBean(oldBookListBean.name, oldBookListBean.list.map {
                            // 旧版的extra为完整地址，直接拿来，就算写进数据库了，刷新详情页后也会被新版的bookId覆盖，
                            NovelMinimal(site = it.site, author = it.author, name = it.name, detail = it.requester.extra)
                        }, 2)
                    }
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

    private fun refresh(novel: Novel) {
        logger.info {
            "refresh <${novel.run { "$site.$author.$name.$detail" }}>"
        }
        if (System.currentTimeMillis() - (novel.checkUpdateTime?.time ?: 0) < config.minTime) {
            // 避免频繁刷新，
            return
        }
        try {
            val context = getNovelContextByName(novel.site)
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
