package cc.aoeiuv020.panovel.detail

import cc.aoeiuv020.panovel.Presenter
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.qrcode.QrCodeManager
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.util.suffixThreadName
import io.reactivex.Observable
import org.jetbrains.anko.*
import java.io.IOException

/**
 *
 * Created by AoEiuV020 on 2017.10.03-18:10:45.
 */
class NovelDetailPresenter(private val id: Long) : Presenter<NovelDetailActivity>(), AnkoLogger {
    private var refresh = false
    private var novel: Novel? = null

    fun start() {
        requestNovelDetail()
    }

    fun refresh() {
        refresh = true
        requestNovelDetail()
    }

    fun share() {
        Observable.fromCallable {
            suffixThreadName("shareBook")
            val url = novelItem.requester.url
            val qrCode = QrCodeManager.generate(novelItem.requester.url)
            url to qrCode
        }.async().subscribe({ (url, qrCode) ->
            view?.showSharedUrl(url, qrCode)
        }, { e ->
            val message = "上传失败，"
            error(message, e)
            view?.showError(message, e)
        }).let { addDisposable(it, 1) }

    }

    private fun requestNovelDetail() {
        doAsync({ e ->
            val message = "获取小说详情失败，"
            if (e !is IOException) {
                Reporter.post(message, e)
            }
            view?.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            val novel = DataManager.getNovelDetail(id)
            view?.showNovelDetail(novel)
            this@NovelDetailPresenter.novel = novel
        }
    }

    fun browse() {
        novel?.let {
            doAsync({ e ->
                val message = "获取小说《${it.name}》<${it.site}, ${it.detail}>详情页地址失败，"
                // 按理说每个网站的extra都是设计好的，可以得到完整地址的，
                Reporter.post(message, e)
                error(message, e)
                view?.runOnUiThread {
                    view?.showError(message, e)
                }
            }) {
                val url = DataManager.getDetailUrl(it)
                uiThread {
                    view?.browse(url)
                }
            }
        } ?: run {
            val message = "还没获取到小说详情，"
            view?.showError(message)
        }
    }
}