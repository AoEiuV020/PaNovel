package cc.aoeiuv020.panovel.history

import cc.aoeiuv020.panovel.base.item.DefaultItemListPresenter
import cc.aoeiuv020.panovel.local.History
import cc.aoeiuv020.panovel.util.async
import io.reactivex.Observable
import org.jetbrains.anko.error

/**
 * 绝大部分照搬书架，
 * Created by AoEiuV020 on 2017.10.15-18:11:15.
 */
class HistoryPresenter : DefaultItemListPresenter<HistoryFragment>() {

    private fun requestHistory() {
        Observable.fromCallable {
            History.list().map { it.novel }
        }.async().subscribe({ list ->
            view?.showNovelList(list)
        }, { e ->
            val message = "获取历史列表失败，"
            error(message, e)
            view?.showError(message, e)
        }).let { addDisposable(it) }
    }

    override fun refresh() {
        requestHistory()
    }
}

