package cc.aoeiuv020.panovel.text

import cc.aoeiuv020.base.jar.ioExecutorService
import cc.aoeiuv020.panovel.Presenter
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.data.NovelManager
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.settings.GeneralSettings
import org.jetbrains.anko.*
import java.util.concurrent.atomic.AtomicInteger

/**
 *
 * Created by AoEiuV020 on 2017.10.03-19:06:50.
 */
class NovelTextPresenter(
        id: Long
) : Presenter<NovelTextActivity>(), AnkoLogger {
    private val novelManager: NovelManager by lazy {
        DataManager.getNovelManager(id)
    }
    private val novel: Novel get() = novelManager.novel

    fun start() {
        requestNovel()
    }

    fun refreshChapterList() {
        requestChapters(true)
    }

    private fun requestNovel() {
        view?.doAsync({ e ->
            val message = "获取小说详情失败，"
            Reporter.post(message, e)
            error(message, e)
            view?.runOnUiThread {
                view?.showNovelNotFound(message, e)
            }
        }) {
            // 初始化novelManager，
            val novel = novelManager.novel
            uiThread {
                view?.showNovel(novel)
            }
        }
    }

    fun requestContent(chapter: NovelChapter, refresh: Boolean): List<String> {
        return novelManager.requestContent(chapter, refresh)
    }

    fun download(fromIndex: Int, count: Int) {
        view?.doAsync({ e ->
            val message = "下载失败，"
            Reporter.post(message, e)
            error(message, e)
            view?.runOnUiThread {
                view?.showError(message, e)
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
            val nextIndex = AtomicInteger(fromIndex)
            val threadsLimit = GeneralSettings.downloadThreadsLimit
            debug {
                "download start <$fromIndex/$size> * $threadsLimit"
            }
            uiThread {
                view?.showDownloadStart(left.get())
            }
            // 同时启动多个线程下载，
            // 判断一下，线程数不要过多，
            repeat(minOf(threadsLimit, left.get())) {
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
                    // 上面判断过，线程数不会过多，一进来index会小于size,
                    while (index < last && view != null) {
                        debug { "${Thread.currentThread().name} downloading $index" }
                        val chapter = chapters[index]
                        if (cachedList.contains(chapter.extra)) {
                            ++exists
                        } else {
                            try {
                                // 方法返回前请求到正文就已经缓存了，
                                novelManager.requestContent(chapter, false)
                                ++downloads
                            } catch (e: Exception) {
                                val message = "缓存<${novel.bookId}.$index>章节失败，"
                                Reporter.post(message, e)
                                error(message, e)
                                ++errors
                            }
                        }
                        val tmpLeft = left.decrementAndGet()
                        val tmpIndex = index
                        uiThread {
                            debug { "download $tmpIndex, left $tmpLeft" }
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

    fun requestChapters(refresh: Boolean = false) {
        view?.doAsync({ e ->
            val message = "加载小说<${novel.bookId}>章节列表失败，"
            Reporter.post(message, e)
            error(message, e)
            view?.runOnUiThread {
                view?.showError(message, e)
            }
        }, ioExecutorService) {
            val list = novelManager.requestChapters(refresh)
            uiThread {
                view?.showChaptersAsc(list)
            }
        }
    }

    fun updateBookshelf(checked: Boolean) {
        view?.doAsync({ e ->
            val message = "${if (novel.bookshelf) "添加" else "删除"}书架《${novel.bookId}》失败，"
            // 这应该是数据库操作出问题，正常情况不会出现才对，
            // 未知异常统一上报，
            Reporter.post(message, e)
            error(message, e)
            view?.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            novelManager.updateBookshelf(checked)
        }
    }

    fun saveReadStatus(novel: Novel) {
        view?.doAsync({ e ->
            val message = "保存<${novel.bookId}>阅读进度失败，"
            Reporter.post(message, e)
            error(message, e)
            view?.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            // 这里更新阅读时间，
            // 考虑到异步顺序不定，如果只有这一处更新阅读时间，可能来不及反应到书架上，
            // 因此得到打开小说时也更新一次阅读时间，
            novelManager.saveReadStatus()
        }
    }

    fun getContentUrl(chapter: NovelChapter): String {
        return novelManager.getContentUrl(chapter)
    }

    fun loadContents() {
        view?.doAsync({ e ->
            val message = "加载小说正文缓存列表失败，"
            Reporter.post(message, e)
            error(message, e)
            view?.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            // 虽然给了异步，但还是要快，因为没给任何提示，
            // 查询小说已经缓存的章节列表，
            val cachedList = novelManager.novelContentsCached()
            uiThread {
                view?.showContents(cachedList)
            }
        }
    }

    fun getDetailUrl(): String {
        return novelManager.getDetailUrl()
    }

}

