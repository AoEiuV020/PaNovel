package cc.aoeiuv020.panovel.history

import cc.aoeiuv020.panovel.Presenter
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.data.NovelManager
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.settings.GeneralSettings
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.error
import org.jetbrains.anko.uiThread

/**
 * 绝大部分照搬书架，
 * Created by AoEiuV020 on 2017.10.15-18:11:15.
 */
class HistoryPresenter : Presenter<HistoryFragment>(), AnkoLogger {

    private fun requestHistory() {
        view?.doAsync({ e ->
            val message = "获取历史列表失败，"
            Reporter.post(message, e)
            error(message, e)
            view?.activity?.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            // TODO: 改成翻页的实现，试试page库之类的，
            val list = DataManager.history(GeneralSettings.historyCount)
            uiThread {
                view?.showNovelList(list)
            }
        }
    }

    fun askUpdate(novelList: List<NovelManager>) {
        view?.doAsync({ e ->
            val message = "询问服务器小说列表更新失败，"
            Reporter.post(message, e)
            error(message, e)
            view?.activity?.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            val resultList = DataManager.askUpdate(novelList)
            uiThread {
                view?.showAskUpdateResult(resultList)
            }
        }
    }

    fun refresh() {
        requestHistory()
    }
}

