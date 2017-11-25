package cc.aoeiuv020.panovel.bookshelf

import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.base.item.BaseItemListPresenter
import cc.aoeiuv020.panovel.base.item.BigItemPresenter
import cc.aoeiuv020.panovel.local.Bookshelf
import cc.aoeiuv020.panovel.local.History
import cc.aoeiuv020.panovel.util.async
import io.reactivex.Observable
import org.jetbrains.anko.error

/**
 *
 * Created by AoEiuV020 on 2017.10.14-21:54.
 */
class BookshelfPresenter : BaseItemListPresenter<BookshelfFragment, BookshelfItemPresenter>() {

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

    @Suppress("UNCHECKED_CAST")
    override fun subPresenter(): BookshelfItemPresenter =
            BookshelfItemPresenter(this)

}

class BookshelfItemPresenter(presenter: BaseItemListPresenter<*, BookshelfItemPresenter>) : BigItemPresenter<BookshelfItemViewHolder>(presenter) {
    private var itemRefreshTime = 0L
    override val refreshTime: Long
        get() = maxOf(super.refreshTime, itemRefreshTime)

    fun forceRefresh(novelItem: NovelItem) {
        itemRefreshTime = System.currentTimeMillis()
        view?.setData(novelItem)
    }
}
