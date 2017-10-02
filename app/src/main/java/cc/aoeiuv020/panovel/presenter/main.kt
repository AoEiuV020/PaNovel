package cc.aoeiuv020.panovel.presenter

import cc.aoeiuv020.panovel.api.NovelContext
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
