package cc.aoeiuv020.panovel.booklist

import cc.aoeiuv020.panovel.Presenter
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.data.entity.BookList
import cc.aoeiuv020.panovel.qrcode.QrCodeManager
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.settings.OtherSettings
import cc.aoeiuv020.panovel.share.Share
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.error
import org.jetbrains.anko.uiThread

/**
 *
 * Created by AoEiuV020 on 2017.11.22-14:31:17.
 */
class BookListFragmentPresenter : Presenter<BookListFragment>() {
    fun refresh() {
        requestBookListList()
    }

    private fun requestBookListList() {
        view?.doAsync({ e ->
            val message = "查询书单列表失败，"
            Reporter.post(message, e)
            error(message, e)
            view?.activity?.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            val list = DataManager.allBookList()
            uiThread {
                view?.showBookListList(list)
            }
        }
    }

    fun shareBookList(bookList: BookList) {
        view?.showUploading()
        view?.doAsync({ e ->
            val message = "上传书单失败，"
            Reporter.post(message, e)
            error(message, e)
            view?.activity?.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            val url = Share.shareBookList(bookList, OtherSettings.shareExpiration)
            val qrCode = QrCodeManager.generate(url)
            uiThread {
                view?.showSharedUrl(url, qrCode)
            }
        }
    }

    fun renameBookList(bookList: BookList, newName: String) {
        view?.doAsync({ e ->
            val message = "书单重命名失败，"
            Reporter.post(message, e)
            error(message, e)
            view?.activity?.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            DataManager.renameBookList(bookList, newName)
            uiThread {
                // 干脆整个刷新，没必要找麻烦，
                view?.refresh()
            }
        }
    }

    fun copyBookList(bookList: BookList, newName: String) {
        view?.doAsync({ e ->
            val message = "书单重命名失败，"
            Reporter.post(message, e)
            error(message, e)
            view?.activity?.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            DataManager.copyBookList(bookList, newName)
            uiThread {
                // 干脆整个刷新，没必要找麻烦，
                view?.refresh()
            }
        }
    }

    fun remove(bookList: BookList) {
        view?.doAsync({ e ->
            val message = "删除书单失败，"
            Reporter.post(message, e)
            error(message, e)
            view?.activity?.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            DataManager.removeBookList(bookList)
            uiThread {
                // 干脆整个刷新，没必要找麻烦，
                view?.refresh()
            }
        }
    }

    fun newBookList(name: String) {
        view?.doAsync({ e ->
            val message = "添加书单失败，"
            Reporter.post(message, e)
            error(message, e)
            view?.activity?.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            DataManager.newBookList(name)
            uiThread {
                // 干脆整个刷新，没必要找麻烦，
                view?.refresh()
            }
        }
    }

    fun removeBookshelf(bookList: BookList) {
        view?.showRemoving()
        doAsync({ e ->
            val message = "从书架移出书单中的小说失败，"
            Reporter.post(message, e)
            error(message, e)
            view?.activity?.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            DataManager.removeFromBookshelf(bookList)
            uiThread {
                view?.showRemoveBookshelfComplete()
            }
        }
    }

    fun addBookshelf(bookList: BookList) {
        view?.showAdding()
        doAsync({ e ->
            val message = "加入书单中的小说到书架失败，"
            Reporter.post(message, e)
            error(message, e)
            view?.activity?.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            DataManager.addToBookshelf(bookList)
            uiThread {
                view?.showAddBookshelfComplete()
            }
        }
    }

}