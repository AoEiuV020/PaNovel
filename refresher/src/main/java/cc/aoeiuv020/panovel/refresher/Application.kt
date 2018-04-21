package cc.aoeiuv020.panovel.refresher

import cc.aoeiuv020.base.jar.error
import cc.aoeiuv020.base.jar.info
import cc.aoeiuv020.base.jar.warn
import cc.aoeiuv020.panovel.api.DetailRequester
import cc.aoeiuv020.panovel.api.NovelContext
import cc.aoeiuv020.panovel.api.Requester
import cc.aoeiuv020.panovel.server.ServerAddress
import cc.aoeiuv020.panovel.server.common.toBean
import cc.aoeiuv020.panovel.server.service.NovelService
import cc.aoeiuv020.panovel.server.service.impl.NovelServiceImpl
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by AoEiuV020 on 2018.04.21-16:05:40.
 */
fun main(args: Array<String>) {
    val ite = args.iterator()
    var address: ServerAddress = ServerAddress.new("localhost:8080")
    var config = Config()
    val bookshelfList = mutableSetOf<String>()

    while (ite.hasNext()) {
        when (ite.next()) {
            "-a" -> {
                address = File(ite.next()).readText().toBean()
            }
            "-h" -> {
                address = ServerAddress.new(ite.next())
            }
            "-b" -> {
                bookshelfList.add(ite.next())
            }
            "-c" -> {
                config = File(ite.next()).readText().toBean()
            }
        }
    }
    Application().start(address = address, config = config, bookshelfList = bookshelfList)
}

data class Config(
        /**
         * 每轮的最小时间，太快了就休息到够，
         */
        val minTime: Long = TimeUnit.MINUTES.toMillis(10),
        /**
         * 目标时间，希望每轮执行时间，
         */
        val targetTime: Long = TimeUnit.MINUTES.toMillis(30),
        /**
         * 一轮最多拿这么多个，
         */
        val maxSize: Int = 300
)

class Application {
    private val logger = LoggerFactory.getLogger(Application::class.java.simpleName)
    private lateinit var service: NovelService
    private var isRunning = false
    fun start(address: ServerAddress, config: Config = Config(), bookshelfList: MutableSet<String>) {
        logger.info {
            "start address: $address"
        }
        logger.info {
            config
        }
        logger.info {
            "bookshelfList: $bookshelfList"
        }
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
                // 需要休息的时间，
                val sleepTime = config.minTime - roundTime
                if (sleepTime > 0) {
                    // 休息，
                    logger.warn {
                        "sleep $sleepTime"
                    }
                    TimeUnit.MILLISECONDS.sleep(sleepTime)
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
                service.needRefreshNovelList(targetCount)
                        .also { lastCount = it.size }
                        .forEach { novel ->
                            logger.info {
                                "refresh ${novel.requesterExtra}"
                            }
                            try {
                                val detailRequester = Requester.deserialization(
                                        novel.requesterType,
                                        novel.requesterExtra
                                ) as DetailRequester
                                val context = NovelContext.getNovelContextByUrl(detailRequester.url)
                                val detail = context.getNovelDetail(detailRequester)
                                val chapters = context.getNovelChaptersAsc(detail.requester)
                                val updateTime = chapters.last().update?.let {
                                    if (it > detail.update) {
                                        it
                                    } else {
                                        detail.update
                                    }
                                } ?: detail.update
                                val hasUpdate = chapters.size > novel.chaptersCount
                                        || updateTime > novel.updateTime
                                novel.updateTime = updateTime
                                novel.chaptersCount = chapters.size
                                novel.modifyTime = Date()
                                if (hasUpdate) {
                                    service.uploadUpdate(novel)
                                } else {
                                    service.touch(novel)
                                }
                            } catch (e: Exception) {
                                logger.error(e) {
                                    novel.requesterExtra
                                }
                            }
                        }
            } catch (e: Exception) {
                logger.error(e) {
                    "请求需要刷新的小说列表失败，"
                }
                isRunning = false
            }
        }
    }

    fun stop() {
        isRunning = false
    }
}

