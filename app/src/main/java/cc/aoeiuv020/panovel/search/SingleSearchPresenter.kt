@file:Suppress("DEPRECATION")


package cc.aoeiuv020.panovel.search

import cc.aoeiuv020.panovel.Presenter
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.report.Reporter
import org.jetbrains.anko.*

/**
 * Created by AoEiuV020 on 2018.05.13-21:59:40.
 */
class SingleSearchPresenter(
        private val site: String
) : Presenter<SingleSearchActivity>(), AnkoLogger {
    fun start() {
        debug { "start," }
    }

    fun pushCookies() {
        debug { "pushCookies," }
        val context = DataManager.getNovelContextByName(site)
        DataManager.pushCookiesToWebView(context)
        view?.doAsync({ e ->
            val message = "传递cookies给浏览器失败，"
            Reporter.post(message, e)
            error(message, e)
            view?.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            DataManager.syncCookies(view)
            uiThread {
                if (view?.getCurrentUrl() == null) {
                    debug {
                        "open home page ${context.homePage}"
                    }
                    view?.openPage(context.homePage)
                }

            }
        }
    }

    fun pullCookies() {
        debug { "pullCookies," }
        val context = DataManager.getNovelContextByName(site)
        DataManager.pullCookiesFromWebView(context)
    }

    fun open(currentUrl: String) {
        debug { "open <$currentUrl>," }
        view?.doAsync({ e ->
            val message = "打开地址<$currentUrl>失败，"
            if (e !is IllegalArgumentException) {
                Reporter.post(message, e)
            }
            error(message, e)
            view?.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            val novel = try {
                DataManager.getNovelFromUrl(site, currentUrl)
            } catch (e: Exception) {
                throw IllegalArgumentException("不支持的地址，", e)
            }
            uiThread {
                view?.openNovelDetail(novel)
            }
        }
    }

    fun removeCookies() {
        debug { "removeCookies," }
        DataManager.removeWebViewCookies()
        view?.doAsync({ e ->
            val message = "删除cookies失败，"
            Reporter.post(message, e)
            error(message, e)
            view?.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            DataManager.syncCookies(view)
            DataManager.removeNovelContextCookies(site)
            uiThread {
                view?.showRemoveCookiesDone()
            }
        }
    }
}