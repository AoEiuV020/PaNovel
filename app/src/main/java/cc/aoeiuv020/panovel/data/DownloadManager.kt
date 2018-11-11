package cc.aoeiuv020.panovel.data

import android.content.Context
import android.view.View
import cc.aoeiuv020.base.jar.ioExecutorService
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.download.DownloadNotificationManager
import cc.aoeiuv020.panovel.download.DownloadingNotificationManager
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.settings.DownloadSettings
import cc.aoeiuv020.panovel.util.safelyShow
import kotlinx.android.synthetic.main.dialog_download_count.view.*
import org.jetbrains.anko.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by AoEiuV020 on 2018.10.06-19:05:33.
 */
class DownloadManager(
        private val ctx: Context
) : AnkoLogger {

    val dnmLocal = object : ThreadLocal<DownloadingNotificationManager>() {
        override fun initialValue(): DownloadingNotificationManager {
            return DownloadingNotificationManager(ctx)
        }
    }

    fun download(novelManager: NovelManager, fromIndex: Int, count: Int) {
        if (count <= 0) return
        val novel = novelManager.novel
        ctx.doAsync({ e ->
            val message = "下载失败，"
            Reporter.post(message, e)
            error(message, e)
            ctx.runOnUiThread {
                // 这种情况没法处理应该往外抛，或者加个监听器，
            }
        }) {
            val chapters = novelManager.requestChapters(false)
            val cachedList = novelManager.novelContentsCached()
            val size = chapters.size
            val last = minOf(size - fromIndex, count) + fromIndex
            var exists = 0
            var downloads = 0
            var errors = 0
            val left = AtomicInteger(last - fromIndex)
            if (left.get() <= 0) return@doAsync
            val nextIndex = AtomicInteger(fromIndex)
            val threadsLimit = maxOf(1, DownloadSettings.downloadThreadsLimit)
            debug {
                "download start <$fromIndex/$size> * $threadsLimit"
            }
            val dfm = DownloadNotificationManager(ctx, novel)
            uiThread {
                dfm.downloadStart(left.get())
            }
            // 同时启动多个线程下载，
            // 判断一下，线程数不要过多，
            repeat(minOf(threadsLimit, left.get())) {
                ctx.doAsync({ e ->
                    val message = "线程下载异常，"
                    Reporter.post(message, e)
                    error(message, e)
                    ctx.runOnUiThread {
                        // 这种情况没法处理应该往外抛，或者加个监听器，
                    }
                }, ioExecutorService) {
                    val thread = Thread.currentThread().name
                    // 每次循环最后再获取，
                    var index = nextIndex.getAndIncrement()
                    // 如果presenter已经detach说明离开了这个页面，不继续下载，
                    // 正在下载的章节不中断，
                    // 上面判断过，线程数不会过多，一进来index会小于size,
                    while (index < last) {
                        debug { "$thread downloading $index" }
                        val chapter = chapters[index]
                        if (cachedList.contains(chapter.extra)) {
                            ++exists
                        } else {
                            try {
                                // 方法返回前请求到正文就已经缓存了，
                                novelManager.requestContent(index, chapter, false)
                                ++downloads
                            } catch (e: Exception) {
                                val message = "缓存<${novel.bookId}.$index>章节失败，"
                                Reporter.post(message, e)
                                error(message, e)
                                ++errors
                            }
                        }
                        val tmpLeft = left.decrementAndGet()
                        uiThread {
                            debug { "download $index, left $tmpLeft" }
                            dfm.downloading(exists, downloads, errors, tmpLeft)
                        }
                        index = nextIndex.getAndIncrement()
                    }
                    uiThread {
                        dfm.downloadComplete(exists, downloads, errors)
                        // 5秒后删除下载结果通知，
                        dfm.cancelNotification(TimeUnit.SECONDS.toMillis(5))
                    }
                }
            }
        }
    }

    // 不能用全局application的Context弹对话框，
    // WindowManager$BadTokenException: Unable to add window -- token null is not for an application
    fun askDownload(ctx: Context, novelManager: NovelManager, currentIndex: Int, fromFirst: Boolean): Boolean {
        val defaultCount = DownloadSettings.downloadCount.takeIf { it >= 0 }
                ?: 50
        ctx.alert {
            titleResource = R.string.download_chapters_count
            val layout = View.inflate(ctx, R.layout.dialog_download_count, null)
            customView = layout
            val etCount = layout.editText.apply {
                setText(defaultCount.toString())
            }
            val rgFrom = layout.rgFrom
            if (fromFirst) {
                rgFrom.check(R.id.rbFromFirst)
            } else {
                rgFrom.check(R.id.rbFromCurrent)
            }
            val cbRemember = layout.checkBox
            fun remember() {
                if (cbRemember.isChecked) {
                    etCount.text.toString().toIntOrNull()?.let {
                        DownloadSettings.downloadCount = it
                    }
                }
            }
            neutralPressed(R.string.all) {
                remember()
                val fromIndex = if (rgFrom.checkedRadioButtonId == R.id.rbFromFirst) {
                    0
                } else {
                    currentIndex
                }
                download(novelManager, fromIndex, Int.MAX_VALUE)
            }
            yesButton {
                remember()
                val count = etCount.text.toString().toIntOrNull() ?: 0
                val realCount = if (count == 0) {
                    Int.MAX_VALUE
                } else {
                    count
                }
                val fromIndex = if (rgFrom.checkedRadioButtonId == R.id.rbFromFirst) {
                    0
                } else {
                    currentIndex
                }
                download(novelManager, fromIndex, realCount)
            }
            cancelButton { }
        }.safelyShow()
        return true
    }

}