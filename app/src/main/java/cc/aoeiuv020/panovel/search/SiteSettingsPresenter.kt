package cc.aoeiuv020.panovel.search

import cc.aoeiuv020.panovel.Presenter
import cc.aoeiuv020.panovel.api.NovelContext
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.util.notNullOrReport
import okhttp3.Cookie
import okhttp3.HttpUrl
import org.jetbrains.anko.*

/**
 * Created by AoEiuV020 on 2019.05.01-19:56:11.
 */
class SiteSettingsPresenter(
        private val site: String
) : Presenter<SiteSettingsActivity>(), AnkoLogger {
    private lateinit var context: NovelContext

    fun start() {
        view?.doAsync({ e ->
            val message = "读取网站失败，"
            Reporter.post(message, e)
            error(message, e)
            view?.runOnUiThread {
                view?.showError(message, e)
                view?.finish()
            }
        }) {
            context = DataManager.getNovelContextByName(site)
            uiThread { view ->
                view.init()
            }
        }
    }

    fun setCookie(input: (String) -> String?, success: () -> Unit) {
        view?.doAsync({ e ->
            val message = "设置Cookie失败，"
            Reporter.post(message, e)
            error(message, e)
            view?.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            val oldCookie = context.cookies.values.joinToString("; ") {
                it.run { "${name()}=${value()}" }
            }
            val newCookie = input(oldCookie)
                    ?: return@doAsync
            val cookieMap = newCookie.split(";").mapNotNull { cookiePair ->
                debug { "pull cookie: <$cookiePair>" }
                // 取出来的cookiePair只有name=value，Cookie.parse一定能通过，也因此可能有超时信息拿不出来的问题，
                Cookie.parse(HttpUrl.parse(context.site.baseUrl).notNullOrReport(), cookiePair)?.let { cookie ->
                    cookie.name() to cookie
                }
            }.toMap()
            context.replaceCookies(cookieMap)
            uiThread {
                success()
            }
        }
    }
}
