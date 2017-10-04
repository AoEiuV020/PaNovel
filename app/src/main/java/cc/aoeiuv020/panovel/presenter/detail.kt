package cc.aoeiuv020.panovel.presenter

import cc.aoeiuv020.panovel.api.DetailRequester
import cc.aoeiuv020.panovel.api.NovelContext
import cc.aoeiuv020.panovel.ui.NovelDetailActivity
import io.reactivex.Observable
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error

/**
 *
 * Created by AoEiuV020 on 2017.10.03-18:10:45.
 */
class NovelDetailPresenter(private val view: NovelDetailActivity, private val requester: DetailRequester) : AnkoLogger {
    fun start() {
        requestNovelDetail()
    }

    private fun requestNovelDetail() {
        Observable.fromCallable {
            NovelContext.getNovelContext(requester.url).getNovelDetail(requester)
        }.async().subscribe({ comicDetail ->
            view.showNovelDetail(comicDetail)
        }, { e ->
            val message = "加载小说详情失败，"
            error(message, e)
            view.showError(message, e)
        })

    }
}