package cc.aoeiuv020.panovel.presenter

import cc.aoeiuv020.panovel.api.ChaptersRequester
import cc.aoeiuv020.panovel.api.NovelContext
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.local.Cache
import cc.aoeiuv020.panovel.ui.NovelDetailActivity
import io.reactivex.Observable
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.error
import java.io.IOException

/**
 *
 * Created by AoEiuV020 on 2017.10.03-18:10:45.
 */
class NovelDetailPresenter(private val view: NovelDetailActivity, private val novelItem: NovelItem) : AnkoLogger {
    private val context: NovelContext by lazy {
        NovelContext.getNovelContextByUrl(novelItem.requester.url)
    }
    private var refresh = false

    fun start() {
        requestNovelDetail()
    }

    fun refresh() {
        refresh = true
        requestNovelDetail()
    }

    private fun requestNovelDetail() {
        val requester = novelItem.requester
        Observable.fromCallable {
            if (refresh) {
                context.getNovelDetail(requester).also { Cache.putDetail(it) }
            } else {
                Cache.getDetail(novelItem)
                        ?: context.getNovelDetail(requester).also { Cache.putDetail(it) }
            }
        }.async().subscribe({ comicDetail ->
            view.showNovelDetail(comicDetail)
        }, { e ->
            val message = "加载小说详情失败，"
            error(message, e)
            view.showError(message, e)
        })
    }

    fun requestChapters(requester: ChaptersRequester) {
        Observable.fromCallable {
            if (refresh) {
                refresh = false
                context.getNovelChaptersAsc(requester)
                        .also { Cache.putChapters(novelItem, it) }
                        .also { debug { "重新获取章节，${it.size}" } }
            } else {
                try {
                    Cache.getChapters(novelItem)?.also { debug { "读取缓存章节，${it.size}" } }
                            ?: context.getNovelChaptersAsc(requester)
                            .also { Cache.putChapters(novelItem, it) }
                            .also { debug { "重新获取章节，${it.size}" } }
                } catch (e: IOException) {
                    Cache.getChapters(novelItem, 0)?.also { debug { "网络有问题，读取缓存不判断超时，${it.size}" } }
                            ?: throw e
                }
            }
        }.async().subscribe({ chapters ->
            view.showNovelChapters(chapters)
        }, { e ->
            val message = "加载小说章节失败，"
            error(message, e)
            view.showError(message, e)
        })
    }
}