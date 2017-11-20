package cc.aoeiuv020.panovel.bookshelf

import cc.aoeiuv020.panovel.Presenter
import cc.aoeiuv020.panovel.api.NovelContext
import cc.aoeiuv020.panovel.api.NovelDetail
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.local.*
import cc.aoeiuv020.panovel.util.async
import io.reactivex.Observable
import org.jetbrains.anko.error

/**
 *
 * Created by AoEiuV020 on 2017.10.14-21:54.
 */
class BookshelfPresenter : Presenter<BookshelfFragment>() {
    private var refreshTime = 0L

    fun start() {
        requestBookshelf()
    }

    private fun requestBookshelf() {
        Observable.fromCallable {
            val history = History.list().map { Pair(it.novel, it.date) }.toMap()
            Bookshelf.list().sortedByDescending { history[it]?.time ?: 0 }
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

    inner class ItemPresenter : Presenter<BookshelfAdapter.ViewHolder>() {
        private var itemRefreshTime = 0L

        fun forceRefresh(novelItem: NovelItem) {
            itemRefreshTime = System.currentTimeMillis()
            view?.setData(novelItem)
        }

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
                this@BookshelfPresenter.view?.showError(message, e)
            }).let { addDisposable(it, 0) }
        }

        fun requestUpdate(novelDetail: NovelDetail) {
            val novelItem = novelDetail.novel
            Observable.fromCallable {
                val detail = Cache.detail.get(novelItem, refreshTime = maxOf(refreshTime, itemRefreshTime))
                        ?: NovelContext.getNovelContextByUrl(novelItem.requester.url)
                        .getNovelDetail(novelItem.requester).also { Cache.detail.put(it.novel, it) }
                detail.update
            }.async().subscribe({ updateTime ->
                view?.showUpdateTime(updateTime)
            }, { e ->
                val message = "读取《${novelItem.bookId}》详情失败，"
                error(message, e)
                this@BookshelfPresenter.view?.showError(message, e)
            }).let { addDisposable(it, 0) }
        }

        fun requestChapters(detail: NovelDetail) {
            Observable.fromCallable {
                val novelItem = detail.novel
                val chapters = Cache.chapters.get(novelItem, refreshTime = maxOf(refreshTime, itemRefreshTime))
                        ?: NovelContext.getNovelContextByUrl(novelItem.requester.url)
                        .getNovelChaptersAsc(detail.requester).also { Cache.chapters.put(novelItem, it) }
                val progress = Progress.load(novelItem).chapter
                Pair(chapters, progress)
            }.async().subscribe({ (chapters, progress) ->
                view?.showChapter(chapters, progress)
            }, { e ->
                val message = "读取《${detail.novel.bookId}》章节失败，"
                error(message, e)
                this@BookshelfPresenter.view?.showError(message, e)
            }).let { addDisposable(it, 1) }
        }
    }

}
