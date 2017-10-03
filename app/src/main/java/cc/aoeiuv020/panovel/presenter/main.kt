package cc.aoeiuv020.panovel.presenter

import cc.aoeiuv020.panovel.App
import cc.aoeiuv020.panovel.api.NovelContext
import cc.aoeiuv020.panovel.api.NovelGenre
import cc.aoeiuv020.panovel.api.NovelSite
import cc.aoeiuv020.panovel.ui.MainActivity
import io.reactivex.Observable
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.error

/**
 *
 * Created by AoEiuV020 on 2017.10.02-21:35:07.
 */
class MainPresenter(private val view: MainActivity) : AnkoLogger {
    fun start() {
        debug { "读取记住的选择，" }
        loadSite()?.also { site ->
            debug { "已有记住网站：${site.name}" }
            view.showSite(site)
            loadGenre(site)?.let { genre ->
                debug { "已有记住分类：${genre.name}" }
                view.showGenre(genre)
            } ?: run {
                debug { "没有记住的分类，" }
            }
        } ?: run {
            debug { "没有记住的网站，弹出网站选择，" }
            requestSites()
        }
    }

    /**
     * 提供记住了的分类选择，
     */
    private fun loadGenre(site: NovelSite): NovelGenre? {
        return try {
            App.ctx.load<NovelGenre>("genre").takeIf {
                NovelContext.getNovelContext(site).check(it.requester.url)
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * 保存记住了的网站选择，
     */
    private fun saveSite(site: NovelSite) {
        App.ctx.save("site", site)
    }

    /**
     * 提供记住了的网站选择，
     */
    private fun loadSite(): NovelSite? {
        return try {
            App.ctx.load("site")
        } catch (_: Exception) {
            null
        }
    }


    fun requestSites() {
        NovelContext.getNovelContexts().map { it.getNovelSite() }.let { sites ->
            view.showSites(sites)
        }
    }

    fun search(site: NovelSite, query: String) {
        debug { "在网站(${site.name})搜索：$query, " }
        NovelContext.getNovelContext(site).searchNovelName(query).let { genre ->
            view.showGenre(genre)
        }
    }

    fun requestGenres(site: NovelSite) {
        saveSite(site)
        Observable.fromCallable {
            NovelContext.getNovelContext(site).getGenres()
        }.async().subscribe({ genres ->
            debug { "加载网站分类列表成功，数量：${genres.size}" }
            view.showGenres(genres)
        }, { e ->
            val message = "加载网站分类列表失败，"
            error(message, e)
            view.showError(message, e)
        })

    }
}
