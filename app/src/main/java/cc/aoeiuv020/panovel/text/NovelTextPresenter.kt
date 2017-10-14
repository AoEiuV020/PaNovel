package cc.aoeiuv020.panovel.text

import cc.aoeiuv020.panovel.Presenter
import cc.aoeiuv020.panovel.api.ChaptersRequester
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.NovelContext
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.local.Cache
import cc.aoeiuv020.panovel.util.async
import io.reactivex.Observable
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.error
import java.io.IOException

/**
 *
 * Created by AoEiuV020 on 2017.10.03-19:06:50.
 */
class NovelTextPresenter(private val novelItem: NovelItem) : Presenter<NovelTextActivity>(), AnkoLogger {
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

    fun download(fromIndex: Int) {
        Observable.fromCallable {
            val detail = Cache.detail.get(novelItem)
                    ?: context.getNovelDetail(novelItem.requester).also { Cache.detail.put(it.novel, it) }
            val chapters = Cache.chapters.get(novelItem)
                    ?: context.getNovelChaptersAsc(detail.requester).also { Cache.chapters.put(novelItem, it) }
            var exists = 0
            var downloads = 0
            var errors = 0
            chapters.drop(fromIndex).forEach { chapter ->
                Cache.text.get(novelItem, chapter.name)?.also { exists++ } ?: try {
                    context.getNovelText(chapter.requester)
                } catch (_: Exception) {
                    errors++
                    null
                }?.also { Cache.text.put(novelItem, it, chapter.name); downloads++ }
            }
            Triple(exists, downloads, errors)
        }.async().subscribe({ (exists, downloads, errors) ->
            debug {
                "download complete <exists, $exists> <downloads, $downloads> <errors, $errors>"
            }
            view?.downloadComplete(exists, downloads, errors)
        }, { e ->
            val message = "下载小说失败，"
            error(message, e)
            view?.showError(message, e)
        })
    }

    private fun requestNovelDetail() {
        val requester = novelItem.requester
        Observable.fromCallable {
            if (refresh) {
                context.getNovelDetail(requester).also { Cache.detail.put(it.novel, it) }
            } else {
                Cache.detail.get(novelItem)
                        ?: context.getNovelDetail(requester).also { Cache.detail.put(it.novel, it) }
            }
        }.async().subscribe({ detail ->
            view?.showDetail(detail)
        }, { e ->
            val message = "加载小说章节详情失败，"
            error(message, e)
            view?.showError(message, e)
        })
    }

    fun requestChapters(requester: ChaptersRequester) {
        Observable.fromCallable {
            if (refresh) {
                context.getNovelChaptersAsc(requester).also { Cache.chapters.put(novelItem, it) }
            } else {
                try {
                    Cache.chapters.get(novelItem)
                            ?: context.getNovelChaptersAsc(requester).also { Cache.chapters.put(novelItem, it) }
                } catch (e: IOException) {
                    error { "网络有问题，读取缓存不判断超时，" }
                    Cache.chapters.get(novelItem, timeout = 0) ?: throw e
                }
            }
        }.async().subscribe({ chapters ->
            view?.showChapters(chapters)
        }, { e ->
            val message = "加载小说章节列表失败，"
            error(message, e)
            view?.showError(message, e)
        })
    }

    fun subPresenter() = NTPresenter()

    inner class NTPresenter : Presenter<NovelTextViewHolder>() {
        fun requestNovelText(chapter: NovelChapter) {
            Observable.fromCallable {
                if (refresh) {
                    // 只刷新一章，
                    refresh = false
                    context.getNovelText(chapter.requester).also { Cache.text.put(novelItem, it, chapter.name) }
                } else {
                    Cache.text.get(novelItem, chapter.name)
                            ?: context.getNovelText(chapter.requester).also { Cache.text.put(novelItem, it, chapter.name) }
                }
            }.async().subscribe({ novelText ->
                view?.showText(novelText)
            }, { e ->
                val message = "加载小说页面失败，"
                error(message, e)
                view?.showError(message, e)
            })
        }
    }
}
