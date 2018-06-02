package cc.aoeiuv020.panovel.search

import cc.aoeiuv020.base.jar.ioExecutorService
import cc.aoeiuv020.panovel.Presenter
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.report.Reporter
import org.jetbrains.anko.debug
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.error
import org.jetbrains.anko.uiThread

/**
 *
 * Created by AoEiuV020 on 2017.10.22-18:18:58.
 */
class FuzzySearchPresenter : Presenter<FuzzySearchActivity>() {
    var name: String? = null
    private var author: String? = null
    private var site: String? = null

    fun singleSite(site: String) {
        this.site = site
    }

    fun search(name: String, author: String? = null) {
        this.name = name
        this.author = author
        searchActual(name, author)
    }

    private fun searchActual(name: String, author: String?) {
        debug { "search <$name, $author>" }
        view?.doAsync({ e ->
            val message = "搜索<$name, $author>失败，"
            Reporter.post(message, e)
            error(message, e)
            view?.runOnUiThread {
                view?.showError(message, e)
                // 失败时不需要通知Complete，反正没做什么，
            }
        }, ioExecutorService) {
            site?.let {
                DataManager.search(it, name, author).let { list ->
                    uiThread {
                        view?.addResult(list)
                    }
                }
            } ?: run {
                DataManager.search(name, author) { list ->
                    uiThread {
                        view?.addResult(list)
                    }
                }
            }
            view?.showOnComplete()
        }
    }
}