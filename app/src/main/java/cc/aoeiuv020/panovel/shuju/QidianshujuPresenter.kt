@file:Suppress("DEPRECATION")


package cc.aoeiuv020.panovel.shuju

import cc.aoeiuv020.panovel.Presenter
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.report.Reporter
import org.jetbrains.anko.*

class QidianshujuPresenter(
    private val site: String
) : Presenter<QidianshujuActivity>(), AnkoLogger {
    fun start() {
        debug { "start," }
        view?.openPage("http://pujie.qidianshuju.com/nlist/0/0/1.html")
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

}