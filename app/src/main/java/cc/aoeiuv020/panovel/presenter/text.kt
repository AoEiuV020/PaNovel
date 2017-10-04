package cc.aoeiuv020.panovel.presenter

import cc.aoeiuv020.panovel.api.DetailRequester
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.NovelContext
import cc.aoeiuv020.panovel.ui.NovelTextActivity
import cc.aoeiuv020.panovel.ui.NovelTextPagerAdapter
import io.reactivex.Observable
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error

/**
 *
 * Created by AoEiuV020 on 2017.10.03-19:06:50.
 */
class NovelTextPresenter(private val view: NovelTextActivity, val requester: DetailRequester, private var index: Int) : AnkoLogger {
    private lateinit var chaptersAsc: List<NovelChapter>
    private lateinit var context: NovelContext

    fun start() {
        Observable.fromCallable {
            NovelContext.getNovelContext(requester.url).also { context = it }
                    .getNovelDetail(requester)
        }.async().subscribe({ detail ->
            chaptersAsc = detail.chaptersAsc
            view.showChapters(chaptersAsc)
        }, { e ->
            val message = "加载小说章节列表失败，"
            error(message, e)
            view.showError(message, e)
        })
    }

    fun subPresenter(view: NovelTextPagerAdapter.ViewHolder) = NTPresenter(view)

    inner class NTPresenter(private val view: NovelTextPagerAdapter.ViewHolder) {
        fun requestNovelText(chapter: NovelChapter) {
            Observable.fromCallable {
                context.getNovelText(chapter.requester)
            }.async().subscribe({ novelText ->
                view.showText(novelText)
            }, { e ->
                val message = "加载小说页面失败，"
                error(message, e)
                view.showError(message, e)
            })
        }
    }
}
