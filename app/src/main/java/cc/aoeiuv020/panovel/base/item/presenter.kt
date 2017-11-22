package cc.aoeiuv020.panovel.base.item

import cc.aoeiuv020.panovel.Presenter
import cc.aoeiuv020.panovel.api.NovelContext
import cc.aoeiuv020.panovel.api.NovelDetail
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.local.Cache
import cc.aoeiuv020.panovel.local.Progress
import cc.aoeiuv020.panovel.local.bookId
import cc.aoeiuv020.panovel.util.async
import io.reactivex.Observable
import org.jetbrains.anko.error

/**
 *
 * Created by AoEiuV020 on 2017.11.22-10:45:36.
 */
abstract class BaseItemListPresenter<V : BaseItemListView> : Presenter<V>() {
    var refreshTime = 0L

    abstract fun refresh()

    fun forceRefresh() {
        refreshTime = System.currentTimeMillis()
        refresh()
    }

    @Suppress("UNCHECKED_CAST")
    open fun <I : BaseItemView> subPresenter(): BaseItemPresenter<I> = DefaultItemPresenter(this)
}

class DefaultItemPresenter<T : BaseItemView>(itemListPresenter: BaseItemListPresenter<*>)
    : BaseItemPresenter<T>(itemListPresenter)

abstract class BaseItemPresenter<T : BaseItemView>(private val itemListPresenter: BaseItemListPresenter<*>) : Presenter<T>() {
    open val refreshTime: Long
        get() = itemListPresenter.refreshTime

    fun requestDetail(novelItem: NovelItem) {
        Observable.fromCallable {
            Cache.detail.get(novelItem)
                    ?: NovelContext.getNovelContextByUrl(novelItem.requester.url)
                    .getNovelDetail(novelItem.requester).also { Cache.detail.put(it.novel, it) }
        }.async().subscribe({ novelDetail ->
            view?.showDetail(novelDetail)
        }, { e ->
            val message = "读取《${novelItem.bookId}》详情失败，"
            error(message, e)
            itemListPresenter.view?.showError(message, e)
        }).let { addDisposable(it, 0) }
    }

    fun requestUpdate(novelDetail: NovelDetail) {
        val novelItem = novelDetail.novel
        Observable.fromCallable {
            val detail = Cache.detail.get(novelItem, refreshTime = refreshTime)
                    ?: NovelContext.getNovelContextByUrl(novelItem.requester.url)
                    .getNovelDetail(novelItem.requester).also { Cache.detail.put(it.novel, it) }
            detail.update
        }.async().subscribe({ updateTime ->
            view?.showUpdateTime(updateTime)
        }, { e ->
            val message = "读取《${novelItem.bookId}》详情失败，"
            error(message, e)
            itemListPresenter.view?.showError(message, e)
        }).let { addDisposable(it, 0) }
    }

    fun requestChapters(detail: NovelDetail) {
        Observable.fromCallable {
            val novelItem = detail.novel
            val chapters = Cache.chapters.get(novelItem, refreshTime = refreshTime)
                    ?: NovelContext.getNovelContextByUrl(novelItem.requester.url)
                    .getNovelChaptersAsc(detail.requester).also { Cache.chapters.put(novelItem, it) }
            val progress = Progress.load(novelItem).chapter
            Pair(chapters, progress)
        }.async().subscribe({ (chapters, progress) ->
            view?.showChapter(chapters, progress)
        }, { e ->
            val message = "读取《${detail.novel.bookId}》章节失败，"
            error(message, e)
            itemListPresenter.view?.showError(message, e)
        }).let { addDisposable(it, 1) }
    }

}