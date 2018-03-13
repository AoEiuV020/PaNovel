package cc.aoeiuv020.panovel.booklist

import cc.aoeiuv020.panovel.Presenter
import cc.aoeiuv020.panovel.local.BookList
import cc.aoeiuv020.panovel.local.BookListData
import cc.aoeiuv020.panovel.local.Settings
import cc.aoeiuv020.panovel.qrcode.QrCodeManager
import cc.aoeiuv020.panovel.share.Share
import cc.aoeiuv020.panovel.util.async
import io.reactivex.Observable
import org.jetbrains.anko.error

/**
 *
 * Created by AoEiuV020 on 2017.11.22-14:31:17.
 */
class BookListFragmentPresenter : Presenter<BookListFragment>() {
    fun refresh() {
        requestBookListList()
    }

    private fun requestBookListList() {
        Observable.fromCallable {
            BookList.list()
        }.async().subscribe({ list ->
            view?.showBookListList(list)
        }, { e ->
            val message = "获取历史列表失败，"
            error(message, e)
            view?.showError(message, e)
        }).let { addDisposable(it, 0) }

    }

    fun shareBookList(bookList: BookListData) {
        view?.showUploading()
        Observable.fromCallable {
            val url = Share.shareBookList(bookList, Settings.shareExpiration)
            val qrCode = QrCodeManager.generate(url)
            url to qrCode
        }.async().subscribe({ (url, qrCode) ->
            view?.showSharedUrl(url, qrCode)
        }, { e ->
            val message = "上传失败，"
            error(message, e)
            view?.showError(message, e)
        }).let { addDisposable(it, 1) }
    }

    fun rename(bookList: BookListData, name: String) {
        Observable.fromCallable {
            BookList.remove(bookList)
            BookList.put(BookListData(name, bookList.list))
        }.async().subscribe({
            view?.refresh()
        }, { e ->
            // 不能作为文件名的符号不可以存在，
            val message = "重命名失败，"
            error(message, e)
            view?.showError(message, e)
        })
    }

}