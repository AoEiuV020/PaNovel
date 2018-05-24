package cc.aoeiuv020.panovel.detail

import cc.aoeiuv020.panovel.Presenter
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.qrcode.QrCodeManager
import cc.aoeiuv020.panovel.report.Reporter
import org.jetbrains.anko.*

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
        novel?.let {
            view?.doAsync({ e ->
                val message = "获取小说《${it.name}》<${it.site}, ${it.detail}>详情页地址失败，"
                // 按理说每个网站的extra都是设计好的，可以得到完整地址的，
                Reporter.post(message, e)
                error(message, e)
                view?.runOnUiThread {
                    view?.showError(message, e)
                }
            }) {
                val url = DataManager.getDetailUrl(it)
                // 简单拼接，这步不可能不问题，
                val qrCode = QrCodeManager.generate(url)
                uiThread {
                    view?.showSharedUrl(url, qrCode)
                }
            }
        } ?: run {
            val message = "还没获取到小说详情，"
            view?.showError(message)
        }
    }

    private fun requestNovelDetail() {
        view?.doAsync({ e ->
            val message = "获取小说详情失败，"
            Reporter.post(message, e)
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
        novel?.also {
            try {
                val url = DataManager.getDetailUrl(it)
                view?.browse(url)
            } catch (e: Exception) {
                val message = "获取小说《${it.name}》<${it.site}, ${it.detail}>详情页地址失败，"
                // 按理说每个网站的extra都是设计好的，可以得到完整地址的，
                Reporter.post(message, e)
                error(message, e)
                view?.showError(message, e)
            }
        } ?: run {
            val message = "还没获取到小说详情，"
            view?.showError(message)
        }
    }

    fun updateBookshelf(novel: Novel) {
        view?.doAsync({ e ->
            val message = "${if (novel.bookshelf) "添加" else "删除"}书架《${novel.name}》失败，"
            // 这应该是数据库操作出问题，正常情况不会出现才对，
            // 未知异常统一上报，
            Reporter.post(message, e)
            error(message, e)
            view?.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            DataManager.updateBookshelf(novel)
        }
    }
}