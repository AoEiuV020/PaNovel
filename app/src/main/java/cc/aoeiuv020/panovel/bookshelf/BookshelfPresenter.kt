package cc.aoeiuv020.panovel.bookshelf

import cc.aoeiuv020.panovel.Presenter
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.util.async
import cc.aoeiuv020.panovel.util.suffixThreadName
import io.reactivex.Observable
import org.jetbrains.anko.error

/**
 *
 * Created by AoEiuV020 on 2017.10.14-21:54.
 */
class BookshelfPresenter : Presenter<BookshelfFragment>() {

    fun start() {
        requestBookshelf()
    }

    private fun requestBookshelf() {
        Observable.fromCallable {
            suffixThreadName("requestBookshelf")
            DataManager.listBookshelf()
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
}

