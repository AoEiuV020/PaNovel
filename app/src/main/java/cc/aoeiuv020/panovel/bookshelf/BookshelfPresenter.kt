package cc.aoeiuv020.panovel.bookshelf

import cc.aoeiuv020.panovel.Presenter
import cc.aoeiuv020.panovel.api.NovelContext
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.local.Bookshelf
import cc.aoeiuv020.panovel.local.Cache
import cc.aoeiuv020.panovel.util.async
import io.reactivex.Observable
import org.jetbrains.anko.error

/**
 *
 * Created by AoEiuV020 on 2017.10.14-21:54.
 */
class BookshelfPresenter : Presenter<BookshelfFragment>() {
    private var refreshTime = 0L

    fun start() {
        requestBookshelf()
    }

    private fun requestBookshelf() {
        Observable.fromCallable {
            Bookshelf.list()
        }.async().subscribe({ list ->
            view?.showNovelList(list)
        }, { e ->
            val message = "获取书架列表失败，"
            error(message, e)
            view?.showError(message, e)
        })
    }

    fun refresh() {
        requestBookshelf()
    }

    fun forceRefresh() {
        refreshTime = System.currentTimeMillis()
        requestBookshelf()
    }

    fun subPresenter(): ItemPresenter = ItemPresenter()

    inner class ItemPresenter : Presenter<BookshelfAdapter.ViewHolder>() {
        fun requestDetail(novelItem: NovelItem) {
            Observable.fromCallable {
                Cache.detail.get(novelItem, timeout = 0)
                        ?: NovelContext.getNovelContextByUrl(novelItem.requester.url)
                        .getNovelDetail(novelItem.requester).also { Cache.detail.put(it.novel, it) }
            }.async().subscribe({ comicDetail ->
                view?.showDetail(comicDetail)
            }, { e ->
                val message = "加载小说详情失败，"
                error(message, e)
                this@BookshelfPresenter.view?.showError(message, e)
            }, {
                Observable.fromCallable {
                    val detail = Cache.detail.get(novelItem, timeout = System.currentTimeMillis() - refreshTime)
                            ?: NovelContext.getNovelContextByUrl(novelItem.requester.url)
                            .getNovelDetail(novelItem.requester).also { Cache.detail.put(it.novel, it) }
                    detail.lastChapter
                }.async().subscribe({ chapter ->
                    view?.showLastChapter(chapter)
                }, { e ->
                    val message = "加载最新章节失败，"
                    error(message, e)
                    this@BookshelfPresenter.view?.showError(message, e)
                })
            })
        }

        fun requestChapterProgress(novelItem: NovelItem) {
            Observable.fromCallable {
                val chapters = Cache.chapters.get(novelItem, timeout = 0)
                val progress = Cache.progress.get(novelItem, timeout = 0)?.chapter ?: 0
                chapters?.get(progress)?.name ?: "null"
            }.async().subscribe({ chapterName ->
                view?.showChapter(chapterName)
            }, { e ->
                val message = "加载小说章节进度失败，"
                error(message, e)
                this@BookshelfPresenter.view?.showError(message, e)
            })
        }
    }

}