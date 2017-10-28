package cc.aoeiuv020.panovel.search

import cc.aoeiuv020.panovel.Presenter
import cc.aoeiuv020.panovel.api.NovelContext
import cc.aoeiuv020.panovel.api.NovelDetail
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.local.Cache
import cc.aoeiuv020.panovel.local.NovelId
import cc.aoeiuv020.panovel.local.bookId
import cc.aoeiuv020.panovel.util.async
import io.reactivex.Observable
import org.jetbrains.anko.debug
import org.jetbrains.anko.error

/**
 *
 * Created by AoEiuV020 on 2017.10.22-18:18:58.
 */
class RefineSearchPresenter : Presenter<RefineSearchActivity>() {
    private var refreshTime = 0L
    private var name: String? = null
    private var author: String? = null

    fun search(name: String, author: String) {
        this.name = name
        this.author = author
        searchActual(name, author)
    }

    fun search(name: String) {
        this.name = name
        searchActual(name, null)
    }

    private fun searchActual(name: String, author: String?) {
        debug { "search <$name, $author>" }
        Observable.create<NovelItem> { em ->
            fun next(novelItem: NovelItem) {
                debug { "search result <${novelItem.name}, ${novelItem.author}>" }
                em.onNext(novelItem)
            }
            NovelContext.getNovelContexts().forEach { context ->
                debug { "search ${context.getNovelSite().name}" }
                try {
                    if (author != null) {
                        // 如果传入了作者，就可以尝试读缓存，
                        Cache.item.get(NovelId(context.getNovelSite().name, author, name))
                                ?: context.getNovelList(context.searchNovelName(name).requester)
                                .firstOrNull { it.novel.name == name }
                                ?.novel
                                ?.let { next(it) }
                    } else {
                        context.getNovelList(context.searchNovelName(name).requester).filter {
                            it.novel.name == name
                        }.forEach { next(it.novel) }
                    }
                } catch (_: Exception) {
                    // 一个网站搜索失败不抛异常，
                }
            }
            em.onComplete()
        }.async().subscribe({ item ->
            view?.addNovel(item)
        }, { e ->
            val message = "搜索小说失败，"
            error(message, e)
            view?.showError(message, e)
        }, {
            view?.showOnComplete()
        }).let { addDisposable(it) }
    }

    fun forceRefresh() {
        refreshTime = System.currentTimeMillis()
        name?.let { nameNonnull ->
            searchActual(nameNonnull, author)
        } ?: view?.showOnComplete()
    }

    fun subPresenter(): ItemPresenter = ItemPresenter()

    inner class ItemPresenter : Presenter<RefineSearchAdapter.ViewHolder>() {
        fun requestDetail(novelItem: NovelItem) {
            Observable.fromCallable {
                Cache.detail.get(novelItem)
                        ?: NovelContext.getNovelContextByUrl(novelItem.requester.url)
                        .getNovelDetail(novelItem.requester).also { Cache.detail.put(it.novel, it) }
            }.async().subscribe({ comicDetail ->
                view?.showDetail(comicDetail)
            }, { e ->
                val message = "读取《${novelItem.bookId}》详情失败，"
                error(message, e)
                this@RefineSearchPresenter.view?.showError(message, e)
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
                this@RefineSearchPresenter.view?.showError(message, e)
            }).let { addDisposable(it, 0) }
        }

        fun requestChapters(detail: NovelDetail) {
            Observable.fromCallable {
                val novelItem = detail.novel
                val chapters = Cache.chapters.get(novelItem, refreshTime = refreshTime)
                        ?: NovelContext.getNovelContextByUrl(novelItem.requester.url)
                        .getNovelChaptersAsc(detail.requester).also { Cache.chapters.put(novelItem, it) }
                val progress = Cache.progress.get(novelItem)?.chapter ?: 0
                Pair(chapters, progress)
            }.async().subscribe({ (chapters, progress) ->
                view?.showChapter(chapters, progress)
            }, { e ->
                val message = "读取《${detail.novel.bookId}》章节失败，"
                error(message, e)
                this@RefineSearchPresenter.view?.showError(message, e)
            }).let { addDisposable(it, 1) }
        }
    }

}