@file:Suppress("DEPRECATION")

// 用到Cookies相关的不少过时方法，兼容低版本需要，

package cc.aoeiuv020.panovel.search

import android.os.Build
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import cc.aoeiuv020.panovel.Presenter
import cc.aoeiuv020.panovel.api.NovelContext
import cc.aoeiuv020.panovel.api.NovelSite
import org.jetbrains.anko.*

/**
 * Created by AoEiuV020 on 2018.05.13-21:59:40.
 */
class SingleSearchPresenter(
        private val site: NovelSite
) : Presenter<SingleSearchActivity>(), AnkoLogger {
    fun start() {
        debug {
            "start,"
        }
    }

    fun pushCookies() {
        debug {
            "pushCookies,"
        }
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        val context = NovelContext.getNovelContextBySite(site)
        val cookies = context.cookies
        // 高版本的设置cookie的回调乱七八糟的，用不上，
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeAllCookies {
                cookies.forEach { (key, value) ->
                    val cookie = "$key=$value"
                    debug {
                        "push cookie: <$cookie>"
                    }
                    context.cookieDomainList().forEach { domain ->
                        cookieManager.setCookie(domain, cookie) {
                            debug {
                                "cookie has been set $it, <$$cookie>"
                            }
                        }
                    }
                }
            }
        } else {
            cookieManager.removeAllCookie()
            cookies.forEach { (key, value) ->
                val cookie = "$key=$value"
                debug {
                    "push cookie: <$cookie>"
                }
                context.cookieDomainList().forEach { domain ->
                    cookieManager.setCookie(domain, cookie)
                }
            }
        }
        view?.doAsync {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cookieManager.flush()
            } else {
                val syncManager = CookieSyncManager.createInstance(view)
                syncManager.sync()
            }
            uiThread {
                if (view?.getCurrentUrl() == null) {
                    debug {
                        "open home page ${site.baseUrl}"
                    }
                    view?.openPage(site.baseUrl)
                }
            }
        }
    }

    fun pullCookies() {
        debug {
            "pullCookies,"
        }
        val context = NovelContext.getNovelContextBySite(site)
        val cookieManager = CookieManager.getInstance()
        val cookiesMap = mutableMapOf<String, String>()
        context.cookieDomainList().forEach { domain ->
            cookieManager.getCookie(domain)?.split(';')?.map {
                it.trim()
            }?.mapNotNull { cookie ->
                debug {
                    "pull cookie: <$cookie>"
                }
                try {
                    val index = cookie.indexOf('=')
                    val key = cookie.substring(0, index)
                    val value = cookie.substring(index + 1)
                    key to value
                } catch (e: Exception) {
                    // 一个cookie处理错误直接无视，
                    error("cookie不合法，<$cookie>,", e)
                    null
                }
            }?.toMap(cookiesMap)
        }
        context.putCookies(cookiesMap)
    }

    fun open(currentUrl: String) {
        debug {
            "open <$currentUrl>,"
        }
        view?.doAsync({ e ->
            val message = "打开地址<$currentUrl>失败，"
            error(message, e)
            view?.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            val novelItem = try {
                NovelContext.getNovelContextBySite(site).getNovelItem(currentUrl)
            } catch (e: Exception) {
                throw IllegalArgumentException("不支持的地址，", e)
            }
            uiThread {
                view?.openNovelDetail(novelItem)
            }
        }
    }

    fun removeCookies() {
        debug {
            "removeCookies,"
        }
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeAllCookies(null)
        } else {
            cookieManager.removeAllCookie()
        }
        view?.doAsync {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cookieManager.flush()
            } else {
                val syncManager = CookieSyncManager.createInstance(view)
                syncManager.sync()
            }
            val context = NovelContext.getNovelContextBySite(site)
            context.removeCookies()
            uiThread {
                view?.showRemoveCookiesDone()
            }
        }
    }
}