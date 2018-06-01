package cc.aoeiuv020.panovel.text

import cc.aoeiuv020.base.jar.ioExecutorService
import cc.aoeiuv020.panovel.Presenter
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.settings.GeneralSettings
import org.jetbrains.anko.*
import java.io.IOException
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**
 *
 * Created by AoEiuV020 on 2017.10.03-19:06:50.
 */
class NovelTextPresenter(private val id: Long) : Presenter<NovelTextActivity>(), AnkoLogger {
    private var refresh = false

    fun start() {
        requestNovel()
    }

    fun refreshChapterList() {
        refresh = true
        requestNovel()
    }

    private fun requestNovel() {
        view?.doAsync({ e ->
            val message = "获取小说详情失败，"
            Reporter.post(message, e)
            error(message, e)
            view?.runOnUiThread {
                view?.showNovelNotFound(message, e)
            }
        }, ioExecutorService) {
            val novel = DataManager.getNovel(id)
            uiThread {
                view?.showNovel(novel)
            }
        }
    }

    fun requestContent(novel: Novel, chapter: NovelChapter, refresh: Boolean): List<String> {
        return DataManager.requestContent(novel, chapter, refresh)
    }

    fun download(novel: Novel, fromIndex: Int) {
        view?.doAsync({ e ->
            val message = "下载失败，"
            Reporter.post(message, e)
            error(message, e)
            view?.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            val chapters = DataManager.requestChapters(novel)
            val cachedList = DataManager.novelContentsCached(novel)
            val size = chapters.size
            var exists = 0
            var downloads = 0
            var errors = 0
            val left = AtomicInteger(size - fromIndex)
            val nextIndex = AtomicInteger(fromIndex)
            val threadsLimit = GeneralSettings.downloadThreadsLimit
            debug {
                "download start <$fromIndex/$size> * $threadsLimit"
            }
            uiThread {
                view?.showDownloadStart(left.get())
            }
            // 同时启动多个线程下载，
            repeat(threadsLimit) {
                view?.doAsync({ e ->
                    val message = "线程下载失败，"
                    Reporter.post(message, e)
                    error(message, e)
                    view?.runOnUiThread {
                        view?.showDownloadError()
                        view?.showError(message, e)
                    }
                }, ioExecutorService) {
                    // 每次循环最后再获取，
                    var index = nextIndex.getAndIncrement()
                    // 如果presenter已经detach说明离开了这个页面，不继续下载，
                    // 正在下载的章节不中断，
                    while (index < size && view != null) {
                        debug { "${Thread.currentThread().name} downloading $index" }
                        val chapter = chapters[index]
                        if (cachedList.contains(chapter.extra)) {
                            ++exists
                        } else {
                            try {
                                // 请求到方法返回前就已经缓存了，
                                DataManager.requestContent(novel, chapter, false)
                                ++downloads
                            } catch (e: Exception) {
                                val message = "缓存章节失败，"
                                Reporter.post(message, e)
                                error(message, e)
                                ++errors
                            }
                        }
                        val tmpLeft = left.decrementAndGet()
                        uiThread {
                            if (tmpLeft == 0) {
                                view?.showDownloadComplete(exists, downloads, errors)
                            } else {
                                view?.showDownloading(exists, downloads, errors, tmpLeft)
                            }
                        }
                        index = nextIndex.getAndIncrement()
                    }
                }
            }
        }
    }

    fun requestChapters(novel: Novel) {
        view?.doAsync({ e ->
            val message = "加载小说章节列表失败，"
            if (e !is IOException) {
                Reporter.post(message, e)
            }
            error(message, e)
            view?.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            val list = DataManager.requestChapters(novel)
            uiThread {
                view?.showChaptersAsc(list)
            }
        }
    }

    fun updateBookshelf(novel: Novel) {
        view?.doAsync({ e ->
            val message = "${if (novel.bookshelf) "添加" else "删除"}书架《${novel.name}》失败，"
            // 这应该是数据库操作出问题，正常情况不会出现才对，
            // 未知异常统一上报，
            Reporter.post(message, e)
            error(message, e)
            view?.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            DataManager.updateBookshelf(novel)
        }
    }

    fun saveReadStatus(novel: Novel) {
        view?.doAsync({ e ->
            val message = "保存阅读进度失败，"
            Reporter.post(message, e)
            error(message, e)
            view?.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            // 这里更新阅读时间，
            // 考虑到异步顺序不定，如果只有这一处更新阅读时间，可能来不及反应到书架上，
            // 因此得到打开小说时也更新一次阅读时间，
            novel.readTime = Date()
            DataManager.updateReadStatus(novel)
        }

    }

}

