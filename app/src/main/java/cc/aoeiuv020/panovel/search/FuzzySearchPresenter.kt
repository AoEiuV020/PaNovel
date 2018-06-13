package cc.aoeiuv020.panovel.search

import cc.aoeiuv020.base.jar.ioExecutorService
import cc.aoeiuv020.panovel.Presenter
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.settings.GeneralSettings
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
                val sites = DataManager.listSites().filter { it.enabled }
                val ite = sites.iterator()
                val futureList = List(GeneralSettings.searchThreadsLimit) {
                    doAsync({ e ->
                        val message = " 搜索线程异常，"
                        // 正常不会到这，
                        Reporter.post(message, e)
                    }) {
                        while (view != null && ite.hasNext()) {
                            val site = synchronized(ite) { ite.next() }
                            debug {
                                "${Thread.currentThread().name} search ${site.name}"
                            }
                            try {
                                val novelManagers = DataManager.search(site.name, name, author).filter {
                                    val novel = it.novel
                                    // 过滤，author为空表示模糊搜索，只要小说名包含，
                                    // author不为空表示精确搜索，要小说名和作者名都匹配，
                                    if (author == null) {
                                        novel.name.contains(name)
                                    } else {
                                        novel.name == name && novel.author == author
                                    }
                                }
                                uiThread {
                                    view?.addResult(novelManagers)
                                }
                            } catch (e: Exception) {
                                val message = "搜索<${site.name}, $name, $author>失败，"
                                Reporter.post(message, e)
                                // 单个网站搜索失败不中断，
                            }
                        }
                    }
                }
                futureList.forEach {
                    // 阻塞，
                    it.get()
                }
            }
            uiThread {
                view?.showOnComplete()
            }
        }
    }
}