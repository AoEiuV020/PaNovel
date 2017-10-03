package cc.aoeiuv020.panovel.presenter

import cc.aoeiuv020.panovel.App
import cc.aoeiuv020.panovel.api.NovelContext
import cc.aoeiuv020.panovel.api.NovelGenre
import cc.aoeiuv020.panovel.ui.NovelListFragment
import io.reactivex.Observable
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.error

/**
 *
 * Created by AoEiuV020 on 2017.10.03-16:12:55.
 */
class NovelListPresenter(private val view: NovelListFragment) : AnkoLogger {
    private lateinit var context: NovelContext
    private lateinit var genre: NovelGenre

    fun requestNovelList(genre: NovelGenre) {
        saveGenre(genre)
        this.genre = genre
        Observable.fromCallable {
            NovelContext.getNovelContext(genre.requester.url).also { context = it }
                    .getNovelList(genre.requester)
        }.async().subscribe({ comicList ->
            view.showNovelList(comicList)
        }, { e ->
            val message = "加载漫画列表失败，"
            error(message, e)
            view.showError(message, e)
        })
    }

    private fun saveGenre(genre: NovelGenre) {
        App.ctx.save("genre", genre)
    }

    fun loadNextPage() {
        debug { "加载下一页，${context.getNovelSite().name}: ${genre.name}" }
        Observable.create<NovelGenre> { em ->
            context.getNextPage(genre)?.let { em.onNext(it) }
            em.onComplete()
        }.async().toList().subscribe({ genres ->
            if (genres.isEmpty()) {
                debug { "没有下一页" }
                view.showYetLastPage()
                return@subscribe
            }
            val genre = genres.first()
            saveGenre(genre)
            view.showUrl(genre.requester.url)
            Observable.fromCallable {
                context.getNovelList(genre.requester)
            }.async().subscribe({ comicList ->
                debug { "展示漫画列表，数量：${comicList.size}" }
                view.addNovelList(comicList)
            }, { e ->
                val message = "加载下一页漫画列表失败，"
                error(message, e)
                view.showError(message, e)
            })
        }, { e ->
            val message = "加载漫画列表一下页地址失败，"
            error(message, e)
            view.showError(message, e)
        })
    }
}