package cc.aoeiuv020.panovel.text

import cc.aoeiuv020.panovel.Presenter
import cc.aoeiuv020.panovel.api.ChaptersRequester
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.NovelContext
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.local.Cache
import cc.aoeiuv020.panovel.local.Settings
import cc.aoeiuv020.panovel.local.id
import cc.aoeiuv020.panovel.util.async
import cc.aoeiuv020.reader.Text
import cc.aoeiuv020.reader.TextRequester
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.error
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

/**
 *
 * Created by AoEiuV020 on 2017.10.03-19:06:50.
 */
class NovelTextPresenter(private val novelItem: NovelItem) : Presenter<NovelTextActivity>(), TextRequester, AnkoLogger {
    private val context: NovelContext by lazy {
        NovelContext.getNovelContextByUrl(novelItem.requester.url)
    }
    private var refresh = false
    private var chapterList: List<NovelChapter> = emptyList()

    fun start() {
        requestNovelDetail()
    }

    fun refreshChapterList() {
        refresh = true
        requestNovelDetail()
    }

    fun download(fromIndex: Int) {
        Observable.create<List<Int>> { em ->
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
                        Cache.text.get(novelItem, chapter.id)?.also { em.onNext(listOf(exists.incrementAndGet(), downloads.get(), errors.get(), left.decrementAndGet())) } ?: try {
                            context.getNovelText(chapter.requester)
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
    }

    private fun requestNovelDetail() {
        val requester = novelItem.requester
        Observable.fromCallable {
            Cache.detail.get(novelItem)
                    ?: context.getNovelDetail(requester).also { Cache.detail.put(it.novel, it) }
        }.async().subscribe({ detail ->
            view?.showDetail(detail)
        }, { e ->
            val message = "加载小说章节详情失败，"
            error(message, e)
            view?.showError(message, e)
        }).let { addDisposable(it, 0) }
    }

    fun requestChapters(requester: ChaptersRequester) {
        Observable.fromCallable {
            if (refresh) {
                context.getNovelChaptersAsc(requester).also { Cache.chapters.put(novelItem, it) }
            } else {
                try {
                    Cache.chapters.get(novelItem)
                            ?: context.getNovelChaptersAsc(requester).also { Cache.chapters.put(novelItem, it) }
                } catch (e: IOException) {
                    error { "网络有问题，读取缓存不判断超时，" }
                    Cache.chapters.get(novelItem, refreshTime = 0) ?: throw e
                }
            }
        }.async().subscribe({ chapters ->
            chapterList = chapters
            view?.showChaptersAsc(chapters)
        }, { e ->
            val message = "加载小说章节列表失败，"
            error(message, e)
            view?.showError(message, e)
        }).let { addDisposable(it, 1) }
    }

    fun getRequester(): TextRequester
            = this

    override fun request(index: Int, refresh: Boolean): Observable<Text>
            = Observable.fromCallable {
        val chapter = chapterList[index]
        if (refresh) {
            debug { "$this refresh $chapter" }
            context.getNovelText(chapter.requester).also { Cache.text.put(novelItem, it, chapter.id) }
        } else {
            debug { "$this load $chapter" }
            Cache.text.get(novelItem, chapter.id)
                    ?: context.getNovelText(chapter.requester).also { Cache.text.put(novelItem, it, chapter.id) }
        }
    }.map { Text(it.textList) }.async()

}
