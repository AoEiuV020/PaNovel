package cc.aoeiuv020.panovel.search

import cc.aoeiuv020.panovel.Presenter
import cc.aoeiuv020.panovel.api.NovelContext
import cc.aoeiuv020.panovel.api.NovelSite
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.data.entity.SiteEnabled
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

/**
 * Created by AoEiuV020 on 2018.05.13-18:29:22.
 */
class SiteChoosePresenter : Presenter<SiteChooseActivity>() {
    fun start() {
        view?.doAsync {
            val siteEnabledMap = DataManager.app.db.siteEnabledDao().list()
                    .map {
                        it.name to it.enabled
                    }.toMap()
            NovelContext.getNovelContexts().map {
                it.getNovelSite()
            }.onEach { site ->
                // 关于是否启用，存在并和现有设置不同则反个enabled,
                if ((siteEnabledMap[site.name] ?: site.enabled) != site.enabled) {
                    site.enabled = !site.enabled
                }
            }.let { novelSiteList ->
                uiThread {
                    view?.showSiteList(novelSiteList)
                }
            }
        }
    }

    fun enabledChange(site: NovelSite, enabled: Boolean) {
        view?.doAsync {
            DataManager.app.db.siteEnabledDao().insert(SiteEnabled(
                    name = site.name,
                    enabled = enabled
            ))
        }
    }
}