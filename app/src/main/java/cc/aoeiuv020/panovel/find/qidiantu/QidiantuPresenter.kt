@file:Suppress("DEPRECATION")


package cc.aoeiuv020.panovel.find.qidiantu

import cc.aoeiuv020.panovel.Presenter
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.report.Reporter
import org.jetbrains.anko.*

class QidiantuPresenter(
    private val site: String
) : Presenter<QidiantuActivity>(), AnkoLogger {
    private lateinit var baseUrl: String
    fun start(url: String?) {
        debug { "start," }
        baseUrl = url ?: "https://www.qidiantu.com/shouding/"
        view?.openPage(baseUrl)
    }

    fun open(currentUrl: String) {
        debug { "open <$currentUrl>," }
        view?.doAsync({ e ->
            val message = "打开地址<$currentUrl>失败，"
            Reporter.post(message, e)
            error(message, e)
            view?.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            val novel = try {
                DataManager.getNovelFromUrl(site, currentUrl)
                    .novel
            } catch (e: Exception) {
                throw IllegalArgumentException("不支持的地址，", e)
            }
            uiThread {
                view?.openNovelDetail(novel)
            }
        }
    }

    fun browse() {
        view?.browse(baseUrl)
    }

}