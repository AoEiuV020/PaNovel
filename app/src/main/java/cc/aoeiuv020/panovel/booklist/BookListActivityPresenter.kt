package cc.aoeiuv020.panovel.booklist

import cc.aoeiuv020.panovel.Presenter
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.data.NovelManager
import cc.aoeiuv020.panovel.report.Reporter
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.error
import org.jetbrains.anko.uiThread

/**
 *
 * Created by AoEiuV020 on 2017.11.22-15:47:37.
 */
class BookListActivityPresenter(private val bookListId: Long) : Presenter<BookListActivity>() {

    fun start() {
        view?.doAsync({ e ->
            val message = "查找书单<$bookListId>失败，"
            Reporter.post(message, e)
            error(message, e)
            view?.runOnUiThread {
                view?.showBookListNotFound(message, e)
            }
        }) {
            val bookList = DataManager.getBookList(bookListId)
            uiThread {
                view?.showBookList(bookList)
            }
        }
    }

    fun refresh() {
        view?.doAsync({ e ->
            val message = "读取书单<$bookListId>中的小说列表失败，"
            Reporter.post(message, e)
            error(message, e)
            view?.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            val list = DataManager.getNovelManagerFromBookList(bookListId)
            uiThread {
                view?.showNovelList(list)
            }
        }
    }

    fun add(novelManager: NovelManager) {
        view?.doAsync({ e ->
            val message = "添加小说到书单<$bookListId>失败，"
            Reporter.post(message, e)
            error(message, e)
            view?.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            novelManager.addToBookList(bookListId)
        }
    }

    fun remove(novelManager: NovelManager) {
        view?.doAsync({ e ->
            val message = "从书单<$bookListId>删除小说失败，"
            Reporter.post(message, e)
            error(message, e)
            view?.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            novelManager.removeFromBookList(bookListId)
        }
    }

    fun addFromHistory() {
        view?.doAsync({ e ->
            val message = "查询历史记录中的小说在书单<$bookListId>中的包含情况失败，"
            Reporter.post(message, e)
            error(message, e)
            view?.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            val list = DataManager.history(50)
            val nameArray = list.map {
                it.novel.bookId
            }.toTypedArray()
            val containsArray = DataManager.inBookList(bookListId, list).toBooleanArray()
            uiThread {
                view?.selectToAdd(list, nameArray, containsArray)
            }
        }
    }

    fun addFromBookshelf() {
        view?.doAsync({ e ->
            val message = "查询书架中的小说在书单<$bookListId>中的包含情况失败，"
            Reporter.post(message, e)
            error(message, e)
            view?.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            val list = DataManager.listBookshelf()
            val nameArray = list.map {
                it.novel.bookId
            }.toTypedArray()
            val containsArray = DataManager.inBookList(bookListId, list).toBooleanArray()
            uiThread {
                view?.selectToAdd(list, nameArray, containsArray)
            }
        }
    }
}