package cc.aoeiuv020.panovel.presenter

import cc.aoeiuv020.panovel.api.DetailRequester
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.NovelContext
import cc.aoeiuv020.panovel.ui.NovelTextActivity
import io.reactivex.Observable
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
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
            requestNovelTexts(false)
        }, { e ->
            val message = "加载漫画章节列表失败，"
            error(message, e)
            view.showError(message, e)
        })
    }

    private fun requestNovelTexts(previous: Boolean) {
        val chapter = chaptersAsc[index]
        debug { "请求${if (previous) "上" else "下"}一话($index.${chapter.name})全图片地址" }
        Observable.fromCallable {
            context.getNovelText(chapter.requester)
        }.async().subscribe({ text ->
            if (previous)
                view.showPreviousChapter(chapter, text)
            else
                view.showNextChapter(chapter, text)
        }, { e ->
            val message = "加载漫画页面失败，"
            error(message, e)
            view.showError(message, e)
        })

    }

    fun requestPreviousChapter() {
        if (index == 0) {
            view.showNoPreviousChapter()
            return
        }
        --index
        requestNovelTexts(true)
    }

    fun requestNextChapter() {
        if (index == chaptersAsc.size - 1) {
            view.showNoNextChapter()
            return
        }
        ++index
        requestNovelTexts(false)
    }
}