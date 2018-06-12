package cc.aoeiuv020.panovel.detail

import cc.aoeiuv020.panovel.Presenter
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.data.NovelManager
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.qrcode.QrCodeManager
import cc.aoeiuv020.panovel.report.Reporter
import org.jetbrains.anko.*

/**
 *
 * Created by AoEiuV020 on 2017.10.03-18:10:45.
 */
class NovelDetailPresenter(
        id: Long
) : Presenter<NovelDetailActivity>(), AnkoLogger {
    // 第一次使用要在异步线程，
    private val novelManager: NovelManager by lazy {
        DataManager.getNovelManager(id)
    }
    private val novel: Novel get() = novelManager.novel

    fun start() {
        requestDetail(false)
    }

    fun refresh() {
        requestDetail(true)
    }

    private fun requestDetail(refresh: Boolean) {
        view?.doAsync({ e ->
            val message = "获取小说<${novel.bookId}>详情失败，"
            Reporter.post(message, e)
            view?.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            // 这里初始化novelManager，
            novelManager.requestDetail(refresh)
            uiThread {
                view?.showNovelDetail(novelManager.novel)
            }
        }
    }

    fun share() {
        view?.doAsync({ e ->
            val message = "获取小说<${novel.bookId}>详情页地址失败，"
            // 按理说每个网站的extra都是设计好的，可以得到完整地址的，
            Reporter.post(message, e)
            error(message, e)
            view?.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            val url = novelManager.getDetailUrl()
            // 简单拼接，这步不可能不问题，
            val qrCode = QrCodeManager.generate(url)
            uiThread {
                view?.showSharedUrl(url, qrCode)
            }
        }
    }

    fun browse() {
        try {
            val url = novelManager.getDetailUrl()
            view?.browse(url)
        } catch (e: Exception) {
            val message = "获取小说《${novel.name}》<${novel.site}, ${novel.detail}>详情页地址失败，"
            // 按理说每个网站的extra都是设计好的，可以得到完整地址的，
            Reporter.post(message, e)
            error(message, e)
            view?.showError(message, e)
        }
    }

    fun updateBookshelf(checked: Boolean) {
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
            novelManager.updateBookshelf(checked)
        }
    }
}