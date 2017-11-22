package cc.aoeiuv020.panovel.bookshelf

import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.base.item.BaseItemListPresenter
import cc.aoeiuv020.panovel.base.item.BaseItemPresenter
import cc.aoeiuv020.panovel.local.Bookshelf
import cc.aoeiuv020.panovel.local.History
import cc.aoeiuv020.panovel.util.async
import io.reactivex.Observable
import org.jetbrains.anko.error

/**
 *
 * Created by AoEiuV020 on 2017.10.14-21:54.
 */
class BookshelfPresenter : BaseItemListPresenter<BookshelfFragment, BookshelfItemViewHolder>() {

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

    override fun refresh() {
        requestBookshelf()
    }

    override fun subPresenter(): BaseItemPresenter<BookshelfItemViewHolder>
            = BookshelfItemPresenter()


    inner class BookshelfItemPresenter : BaseItemPresenter<BookshelfItemViewHolder>(this@BookshelfPresenter) {
        private var itemRefreshTime = 0L
        override val refreshTime: Long
            get() = maxOf(super.refreshTime, itemRefreshTime)

        fun forceRefresh(novelItem: NovelItem) {
            itemRefreshTime = System.currentTimeMillis()
            view?.setData(novelItem)
        }
    }

}
