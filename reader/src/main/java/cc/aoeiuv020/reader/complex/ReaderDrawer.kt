package cc.aoeiuv020.reader.complex

import android.graphics.Canvas
import android.support.v4.util.LruCache
import android.text.TextPaint
import cc.aoeiuv020.pager.Pager
import cc.aoeiuv020.pager.PagerDrawer
import cc.aoeiuv020.pager.Size
import cc.aoeiuv020.reader.Novel
import cc.aoeiuv020.reader.Text
import cc.aoeiuv020.reader.TextRequester
import org.jetbrains.anko.*

/**
 *
 * Created by AoEiuV020 on 2017.12.03-04:09:17.
 */
class ReaderDrawer(private val reader: ComplexReader, private val novel: Novel, private val requester: TextRequester)
    : PagerDrawer(), AnkoLogger {
    private val pagesCache: LruCache<Int, List<Page>> = LruCache(8)
    private lateinit var textPaint: TextPaint
    var chapterIndex = 0
    private var pageIndex = 0

    override fun attach(pager: Pager, backgroundSize: Size, contentSize: Size) {
        super.attach(pager, backgroundSize, contentSize)

        // 清空分页的缓存，
        pagesCache.evictAll()

        textPaint = TextPaint().apply {
            textSize = reader.ctx.sp(reader.config.textSize).toFloat()
        }
    }

    override fun drawCurrentPage(background: Canvas, content: Canvas) {
        background.drawColor(reader.config.backgroundColor)

        if (pager == null) {
            return
        }

        if (chapterIndex !in reader.chapterList.indices) {
            return
        }

        val pages = pagesCache[chapterIndex]
        if (pages == null) {
            debug { "chapter $chapterIndex pages null" }
            request(chapterIndex)
            // TODO 显示正在排版，
            return
        }

        if (pages.isEmpty()) {
            debug { "chapter $chapterIndex pages empty" }
            // TODO 显示本章空内容，
            return
        }

        val page = pages.first()
        var y = 0f
        page.lines.forEach { line ->
            y += textPaint.textSize
            content.drawText(line, 0f, y, textPaint)
            y += reader.config.lineSpacing
        }
    }

    private fun request(requestIndex: Int) {
        val text = requester.request(requestIndex)
        val pages = typesetting(text)
        pagesCache.put(requestIndex, pages)
        if (requestIndex == chapterIndex) {
            pager?.refresh()
        }
    }

    private fun typesetting(text: Text): List<Page> {
        val pages = mutableListOf<Page>()
        var height = 0
        val lines = mutableListOf<String>()
        text.list.forEach {
            val paragraph = "　　" + it
            var start = 0
            var count: Int
            while (start < paragraph.length) {
                // TODO 段间距没考虑，
                height += textPaint.textSize.toInt() + reader.ctx.dip(reader.config.lineSpacing)
                if (height > contentSize.height) {
                    height = 0
                    pages.add(Page(ArrayList(lines)))
                    lines.clear()
                }
                count = textPaint.breakText(paragraph.substring(start), true, contentSize.width.toFloat(), null)
                val line = paragraph.substring(start, start + count)
                info {
                    "<$start, $count, $height> $line"
                }
                lines.add(line)
                start += count
            }
        }
        debug { "pages size = ${pages.size}" }
        pages.forEach {
            debug { it }
        }
        return pages
    }

    override fun scrollToPrev(): Boolean {
        if (chapterIndex - 1 in reader.chapterList.indices) {
            chapterIndex--
            return true
        }
        return false
    }

    override fun scrollToNext(): Boolean {
        if (chapterIndex + 1 in reader.chapterList.indices) {
            chapterIndex++
            return true
        }
        return false
    }
}