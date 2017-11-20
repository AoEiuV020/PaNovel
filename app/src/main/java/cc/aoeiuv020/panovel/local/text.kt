package cc.aoeiuv020.panovel.local

import android.os.Handler
import android.os.Looper
import cc.aoeiuv020.panovel.App
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelContext
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.util.async
import cc.aoeiuv020.panovel.util.notify
import io.reactivex.Observable
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error

/**
 * 导出小说文本，
 * Created by AoEiuV020 on 2017.11.20-17:03:21.
 */
object Text : LocalSource, AnkoLogger {
    private val handler = Handler(Looper.getMainLooper())
    private var index = 0
    fun exportExistsChapterToTextFile(novelItem: NovelItem) {
        index++
        val exportingRunnable = object : Runnable {
            var export = 0
            var skip = 0
            var left = 0
            var error = false
            fun set(export: Int, skip: Int, left: Int) {
                this.export = export
                this.skip = skip
                this.left = left
            }

            fun error() {
                error = true
            }

            override fun run() {
                val ctx = App.ctx
                when {
                    error -> ctx.notify(10000 + index, ctx.getString(R.string.export_error_placeholder, export, skip, left), novelItem.name)
                    left == 0 -> ctx.notify(10000 + index, ctx.getString(R.string.export_complete_placeholder, export, skip), novelItem.name)
                    else -> ctx.notify(10000 + index, ctx.getString(R.string.exporting_placeholder, export, skip, left), novelItem.name)
                }
            }
        }
        Observable.create<List<Int>> { em ->
            val context = NovelContext.getNovelContextByUrl(novelItem.requester.url)
            val detail = Cache.detail.get(novelItem)
                    ?: context.getNovelDetail(novelItem.requester).also { Cache.detail.put(it.novel, it) }
            val chapters = Cache.chapters.get(novelItem)
                    ?: context.getNovelChaptersAsc(detail.requester).also { Cache.chapters.put(novelItem, it) }
            val output = openFile("${novelItem.bookId}.txt").outputStream().bufferedWriter()
            val size = chapters.size
            var export = 0
            var skip = 0
            var left = size
            em.onNext(listOf(export, skip, left))
            chapters.forEach { chapter ->
                output.write(chapter.name)
                output.newLine()
                val novelText = Cache.text.get(novelItem, chapter.id)
                left--
                if (novelText == null) {
                    skip++
                } else {
                    export++
                    novelText.textList.forEach {
                        output.write("　　" + it)
                        output.newLine()
                    }
                }
                em.onNext(listOf(export, skip, left))
            }
        }.async().subscribe({ (export, skip, left) ->
            exportingRunnable.set(export, skip, left)
            handler.postDelayed(exportingRunnable, 100)
        }, { e ->
            val message = "导出小说失败，"
            error(message, e)
            exportingRunnable.error()
            handler.postDelayed(exportingRunnable, 100)
        })
    }
}