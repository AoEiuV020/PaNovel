package cc.aoeiuv020.panovel.search

import cc.aoeiuv020.panovel.api.NovelContext
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.base.item.DefaultItemListPresenter
import cc.aoeiuv020.panovel.local.Cache
import cc.aoeiuv020.panovel.local.NovelId
import cc.aoeiuv020.panovel.util.async
import io.reactivex.Observable
import org.jetbrains.anko.debug
import org.jetbrains.anko.error
import org.jetbrains.anko.verbose

/**
 *
 * Created by AoEiuV020 on 2017.10.22-18:18:58.
 */
class RefineSearchPresenter : DefaultItemListPresenter<RefineSearchActivity>() {
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
                        (Cache.item.get(NovelId(context.getNovelSite().name, author, name))
                                ?: context.getNovelList(context.searchNovelName(name).requester)
                                .firstOrNull { it.novel.name == name && it.novel.author == author }
                                ?.novel)
                                ?.let { next(it) }
                    } else {
                        context.getNovelList(context.searchNovelName(name).requester).filter {
                            verbose { it.novel }
                            it.novel.name == name
                        }.forEach { next(it.novel) }
                    }
                } catch (e: Exception) {
                    // 一个网站搜索失败不抛异常，
                    error { e }
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

    override fun refresh() {
        name?.let { nameNonnull ->
            searchActual(nameNonnull, author)
        } ?: view?.showOnComplete()
    }
}