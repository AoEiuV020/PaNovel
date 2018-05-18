package cc.aoeiuv020.panovel.detail

import cc.aoeiuv020.panovel.Presenter
import cc.aoeiuv020.panovel.api.NovelContext
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.api.Requester
import cc.aoeiuv020.panovel.local.Cache
import cc.aoeiuv020.panovel.qrcode.QrCodeManager
import cc.aoeiuv020.panovel.util.async
import cc.aoeiuv020.panovel.util.suffixThreadName
import io.reactivex.Observable
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.error
import java.io.IOException

/**
 *
 * Created by AoEiuV020 on 2017.10.03-18:10:45.
 */
class NovelDetailPresenter(private val novelItem: NovelItem) : Presenter<NovelDetailActivity>(), AnkoLogger {
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

    fun share() {
        Observable.fromCallable {
            suffixThreadName("shareBook")
            val url = novelItem.requester.url
            val qrCode = QrCodeManager.generate(novelItem.requester.url)
            url to qrCode
        }.async().subscribe({ (url, qrCode) ->
            view?.showSharedUrl(url, qrCode)
        }, { e ->
            val message = "上传失败，"
            error(message, e)
            view?.showError(message, e)
        }).let { addDisposable(it, 1) }

    }

    private fun requestNovelDetail() {
        val requester = novelItem.requester
        Observable.fromCallable {
            suffixThreadName("requestNovelDetail")
            if (refresh) {
                context.getNovelDetail(requester).also { Cache.detail.put(it.novel, it) }
            } else {
                Cache.detail.get(novelItem)
                        ?: context.getNovelDetail(requester).also { Cache.detail.put(it.novel, it) }
            }
        }.async().subscribe({ comicDetail ->
            view?.showNovelDetail(comicDetail)
        }, { e ->
            val message = "加载小说详情失败，"
            error(message, e)
            view?.showError(message, e)
        }).let { addDisposable(it, 0) }
    }

    fun requestChapters(requester: Requester) {
        Observable.fromCallable {
            // 还有其他地方有requestChapters，所以多加个后缀，
            suffixThreadName("requestChaptersDetail")
            if (refresh) {
                refresh = false
                context.getNovelChaptersAsc(requester)
                        .also { Cache.chapters.put(novelItem, it) }
                        .also { debug { "重新获取章节，${it.size}" } }
            } else {
                try {
                    Cache.chapters.get(novelItem)?.also { debug { "读取缓存章节，${it.size}" } }
                            ?: context.getNovelChaptersAsc(requester)
                                    .also { Cache.chapters.put(novelItem, it) }
                                    .also { debug { "重新获取章节，${it.size}" } }
                } catch (e: IOException) {
                    Cache.chapters.get(novelItem, refreshTime = 0)?.also { debug { "网络有问题，读取缓存不判断超时，${it.size}" } }
                            ?: throw e
                }
            }.asReversed().let { ArrayList(it) }
        }.async().subscribe({ chapters ->
            view?.showNovelChaptersDesc(chapters)
        }, { e ->
            val message = "加载小说章节失败，"
            error(message, e)
            view?.showError(message, e)
        }).let { addDisposable(it, 1) }
    }
}