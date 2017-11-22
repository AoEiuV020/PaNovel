package cc.aoeiuv020.panovel.booklist

import cc.aoeiuv020.panovel.base.item.DefaultItemListPresenter
import cc.aoeiuv020.panovel.local.BookList
import cc.aoeiuv020.panovel.util.async
import io.reactivex.Observable
import org.jetbrains.anko.error

/**
 *
 * Created by AoEiuV020 on 2017.11.22-15:47:37.
 */
class BookListActivityPresenter(private val bookListName: String) : DefaultItemListPresenter<BookListActivity>() {

    private fun requestHistory() {
        Observable.fromCallable {
            val bookListData = BookList.get(bookListName)
                    ?: throw Exception("书单不存在")
            bookListData.list
        }.async().subscribe({ list ->
            view?.showNovelList(list)
        }, { e ->
            val message = "获取书单小说列表失败，"
            error(message, e)
            view?.showError(message, e)
        }).let { addDisposable(it) }
    }

    override fun refresh() {
        requestHistory()
    }
}