package cc.aoeiuv020.panovel.text

import cc.aoeiuv020.panovel.Presenter
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.report.Reporter
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.error
import org.jetbrains.anko.uiThread
import java.io.IOException

/**
 *
 * Created by AoEiuV020 on 2017.10.03-19:06:50.
 */
class NovelTextPresenter(private val id: Long) : Presenter<NovelTextActivity>(), AnkoLogger {
    private var refresh = false
    private var chapterList: List<NovelChapter> = emptyList()

    fun start() {
        requestNovel()
    }

    fun refreshChapterList() {
        refresh = true
        requestNovel()
    }

    private fun requestNovel() {
        doAsync({ e ->
            val message = "获取小说详情失败，"
            if (e !is IOException) {
                Reporter.post(message, e)
            }
            error(message, e)
            view?.runOnUiThread {
                view?.showNovelNotFound(message, e)
            }
        }) {
            val novel = DataManager.getNovel(id)
            view?.showNovel(novel)
        }
    }

    fun download(fromIndex: Int) {
/*
        Observable.create<List<Int>> { em ->
            suffixThreadName("download")
            val detail = Cache.detail.get(novelItem)
                    ?: context.getNovelDetail(novelItem.requester).also { Cache.detail.put(it.novel, it) }
            val chapters = Cache.chapters.get(novelItem)
                    ?: context.getNovelChaptersAsc(detail.requester).also { Cache.chapters.put(novelItem, it) }
            val size = chapters.size
            val exists = AtomicInteger()
            val downloads = AtomicInteger()
            val errors = AtomicInteger()
            val left = AtomicInteger(size - fromIndex)
            val nextIndex = AtomicInteger(fromIndex)
            debug {
                "download start <$fromIndex/$size>"
            }
            // 同时启动多个线程下载，发射到上面总的em,
            repeat(Settings.downloadThreadCount) {
                // io线程下载，线程数不算在Settings.asyncThreadCount里，
                Observable.fromCallable {
                    var index = nextIndex.getAndIncrement()
                    // 如果总的em已经取消订阅则不再继续，
                    // 取消订阅时正在下载的不中断，
                    while (index < size && !em.isDisposed) {
                        debug { "${Thread.currentThread().name} downloading $index" }
                        val chapter = chapters[index]
                        Cache.text.get(novelItem, chapter.id)?.also { em.onNext(listOf(exists.incrementAndGet(), downloads.get(), errors.get(), left.decrementAndGet())) }
                                ?: try {
                                    context.getNovelContent(chapter.requester)
                                } catch (_: Exception) {
                                    em.onNext(listOf(exists.get(), downloads.get(), errors.incrementAndGet(), left.decrementAndGet()))
                                    null
                                }?.also { Cache.text.put(novelItem, it, chapter.id); em.onNext(listOf(exists.get(), downloads.incrementAndGet(), errors.get(), left.decrementAndGet())) }
                        index = nextIndex.getAndIncrement()
                    }
                }.subscribeOn(Schedulers.io()).subscribe()
            }
        }.async().subscribe({ (exists, downloads, errors, left) ->
            debug {
                "downloaded <exists, $exists> <downloads, $downloads> <errors, $errors> <left, $left>"
            }
            if (left == 0) {
                view?.showDownloadComplete(exists, downloads, errors)
            } else {
                view?.showDownloading(exists, downloads, errors, left)
            }
        }, { e ->
            val message = "下载小说失败，"
            error(message, e)
            view?.showError(message, e)
        }).let { addDisposable(it, 2) }
*/
    }

    fun requestChapters(novel: Novel) {
        doAsync({ e ->
            val message = "加载小说章节列表失败，"
            Reporter.post(message, e)
            error(message, e)
            view?.showError(message, e)
        }) {
            val list = DataManager.requestChapters(novel)
            uiThread {
                view?.showChaptersAsc(list)
            }
        }
    }

    fun updateBookshelf(novel: Novel) {
        doAsync({ e ->
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

    fun requestContent(novel: Novel, chapter: NovelChapter, refresh: Boolean): List<String> {
        return DataManager.requestContent(novel, chapter, refresh)
    }

}
