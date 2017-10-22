package cc.aoeiuv020.panovel.history

import cc.aoeiuv020.panovel.Presenter
import cc.aoeiuv020.panovel.api.NovelContext
import cc.aoeiuv020.panovel.api.NovelDetail
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.local.Cache
import cc.aoeiuv020.panovel.local.History
import cc.aoeiuv020.panovel.local.bookId
import cc.aoeiuv020.panovel.util.async
import io.reactivex.Observable
import org.jetbrains.anko.error

/**
 * 绝大部分照搬书架，
 * Created by AoEiuV020 on 2017.10.15-18:11:15.
 */
class HistoryPresenter : Presenter<HistoryFragment>() {
    private var refreshTime = 0L

    fun start() {
        requestBookshelf()
    }

    private fun requestBookshelf() {
        Observable.fromCallable {
            History.list().map { it.novel }
        }.async().subscribe({ list ->
            view?.showNovelList(list)
        }, { e ->
            val message = "获取书架列表失败，"
            error(message, e)
            view?.showError(message, e)
        }).let { addDisposable(it) }
    }

    fun refresh() {
        requestBookshelf()
    }

    fun forceRefresh() {
        refreshTime = System.currentTimeMillis()
        requestBookshelf()
    }

    fun subPresenter(): ItemPresenter = ItemPresenter()

    inner class ItemPresenter : Presenter<HistoryAdapter.ViewHolder>() {
        fun requestDetail(novelItem: NovelItem) {
            Observable.fromCallable {
                Cache.detail.get(novelItem)
                        ?: NovelContext.getNovelContextByUrl(novelItem.requester.url)
                        .getNovelDetail(novelItem.requester).also { Cache.detail.put(it.novel, it) }
            }.async().subscribe({ comicDetail ->
                view?.showDetail(comicDetail)
            }, { e ->
                val message = "读取《${novelItem.bookId}》详情失败，"
                error(message, e)
                this@HistoryPresenter.view?.showError(message, e)
            }).let { addDisposable(it, 0) }
        }

        fun requestChapters(detail: NovelDetail) {
            Observable.fromCallable {
                val novelItem = detail.novel
                val chapters = Cache.chapters.get(novelItem, timeout = System.currentTimeMillis() - refreshTime)
                        ?: NovelContext.getNovelContextByUrl(novelItem.requester.url)
                        .getNovelChaptersAsc(detail.requester).also { Cache.chapters.put(novelItem, it) }
                val progress = Cache.progress.get(novelItem)?.chapter ?: 0
                Pair(chapters, progress)
            }.async().subscribe({ (chapters, progress) ->
                view?.showChapter(chapters, progress)
            }, { e ->
                val message = "读取《${detail.novel.bookId}》章节失败，"
                error(message, e)
                this@HistoryPresenter.view?.showError(message, e)
            }).let { addDisposable(it, 1) }
        }
    }

}

