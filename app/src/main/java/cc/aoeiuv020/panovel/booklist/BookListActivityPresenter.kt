package cc.aoeiuv020.panovel.booklist

import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.base.item.DefaultItemListPresenter
import cc.aoeiuv020.panovel.local.BookList
import cc.aoeiuv020.panovel.local.BookListData
import cc.aoeiuv020.panovel.util.async
import io.reactivex.Observable
import org.jetbrains.anko.error

/**
 *
 * Created by AoEiuV020 on 2017.11.22-15:47:37.
 */
class BookListActivityPresenter(private val bookListName: String) : DefaultItemListPresenter<BookListActivity>() {
    private val bookListData: BookListData by lazy {
        BookList.get(bookListName)
                ?: throw Exception("书单不存在")
    }

    private fun requestHistory() {
        Observable.fromCallable {
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

    fun add(novelItem: NovelItem) {
        bookListData.list.add(novelItem)
    }

    fun remove(position: Int) {
        bookListData.list.removeAt(position)
    }

    fun save() {
        Observable.fromCallable {
            BookList.put(bookListData)
            bookListData.list.size
        }.async().subscribe({ size ->
            view?.showSaveComplete(size)
        }, { e ->
            val message = "保存书单小说列表失败，"
            error(message, e)
            view?.showError(message, e)
        }).let { addDisposable(it) }
    }
}