package cc.aoeiuv020.panovel.bookshelf

import cc.aoeiuv020.panovel.Presenter
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.report.Reporter
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.error
import org.jetbrains.anko.uiThread

/**
 *
 * Created by AoEiuV020 on 2017.10.14-21:54.
 */
class BookshelfPresenter : Presenter<BookshelfFragment>() {

    fun start() {
        requestBookshelf()
    }

    private fun requestBookshelf() {
        view?.doAsync({ e ->
            val message = "获取书架列表失败，"
            Reporter.post(message, e)
            error(message, e)
            view?.activity?.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            val list = DataManager.listBookshelf()
            uiThread {
                view?.showNovelList(list)
            }
        }
    }

    fun refresh() {
        requestBookshelf()
    }
}

