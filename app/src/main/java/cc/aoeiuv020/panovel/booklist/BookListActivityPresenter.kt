package cc.aoeiuv020.panovel.booklist

import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.base.item.DefaultItemListPresenter
import cc.aoeiuv020.panovel.local.BookList
import cc.aoeiuv020.panovel.local.BookListData
import cc.aoeiuv020.panovel.local.NovelHistory
import cc.aoeiuv020.panovel.util.async
import cc.aoeiuv020.panovel.util.suffixThreadName
import io.reactivex.Observable
import org.jetbrains.anko.error
import java.util.*

/**
 *
 * Created by AoEiuV020 on 2017.11.22-15:47:37.
 */
class BookListActivityPresenter(private val bookListName: String) : DefaultItemListPresenter<BookListActivity>() {
    private lateinit var list: MutableList<NovelHistory>
    private val bookListData: BookListData by lazy {
        BookList.get(bookListName)
                ?: throw Exception("书单不存在")
    }

    private fun requestHistory() {
        Observable.fromCallable {
            suffixThreadName("requestHistory")
            list = ArrayList(bookListData.list.map { NovelHistory(it, Date(0)) })
            list
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

    fun addOk() {
        view?.showNovelList(list)
    }

    fun contains(novelItem: NovelItem)
            = list.any { it.novel == novelItem }

    fun add(novelItem: NovelItem) {
        list.add(NovelHistory(novelItem))
    }

    fun remove(novelItem: NovelItem) {
        list.remove(NovelHistory(novelItem))
    }

    fun remove(position: Int) {
        list.removeAt(position)
    }

    fun saveBookList() {
        Observable.fromCallable {
            suffixThreadName("saveBookList")
            bookListData.list.clear()
            bookListData.list.addAll(list.map { it.novel })
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