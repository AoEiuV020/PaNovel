package cc.aoeiuv020.reader.complex

import android.graphics.Canvas
import android.support.v4.util.LruCache
import android.text.TextPaint
import cc.aoeiuv020.pager.Pager
import cc.aoeiuv020.pager.PagerDrawer
import cc.aoeiuv020.pager.Size
import cc.aoeiuv020.reader.*
import org.jetbrains.anko.*

/**
 *
 * Created by AoEiuV020 on 2017.12.03-04:09:17.
 */
class ReaderDrawer(private val reader: ComplexReader, private val novel: Novel, private val requester: TextRequester)
    : PagerDrawer(), AnkoLogger {
    private val pagesCache: LruCache<Int, List<Page>?> = LruCache(8)
    private lateinit var textPaint: TextPaint
    var chapterIndex = 0
    var pageIndex = 0

    init {
        reader.config.listeners.add(object : ConfigChangedListener {
            override fun onConfigChanged(name: ReaderConfigName) {
                when (name) {
                    ReaderConfigName.AnimMode -> {
                        reader.config.animMode?.let { pager?.animMode = it }
                    }
                    else -> {
                        reset()
                        pager?.refresh()
                    }
                }
            }
        })
    }

    override fun attach(pager: Pager, backgroundSize: Size, contentSize: Size) {
        super.attach(pager, backgroundSize, contentSize)

        reset()
    }

    private fun reset() {
        // 清空分页的缓存，
        pagesCache.evictAll()

        textPaint = TextPaint().apply {
            textSize = reader.ctx.sp(reader.config.textSize).toFloat()
        }
    }

    override fun drawCurrentPage(background: Canvas, content: Canvas) {
        debug { "drawCurrentPage <$chapterIndex, $pageIndex>" }
        background.drawColor(reader.config.backgroundColor)

        if (pager == null) {
            warn { "pager is null" }
            return
        }

        if (chapterIndex !in reader.chapterList.indices) {
            warn { "chapter index out of bounds <$chapterIndex/${reader.chapterList.size}>" }
            return
        }

        val pages = pagesCache[chapterIndex]
        if (pages == null) {
            warn { "chapter $chapterIndex pages null" }
            request(chapterIndex)
            // TODO 显示正在排版，
            return
        }

        if (pages.isEmpty()) {
            warn { "chapter $chapterIndex pages empty" }
            // TODO 显示本章空内容，
            return
        }

        // 调小字体有可能出现页数变少，
        if (pageIndex > pages.lastIndex) {
            pageIndex = pages.lastIndex
        }

        // 往上翻章节时pageIndex会是负数，表示倒数，
        while (pageIndex < 0) {
            pageIndex += pages.size
        }

        val page = pages[pageIndex]
        var y = 0
        val paragraphSpacing = reader.ctx.dip(reader.config.paragraphSpacing)
        page.lines.forEach { line ->
            verbose { "draw height $y/${content.height}" }
            when (line) {
                is String -> {
                    y += textPaint.textSize.toInt()
                    content.drawText(line, 0f, y.toFloat(), textPaint)
                }
                is ParagraphSpacing -> y += paragraphSpacing
            }
            y += reader.ctx.dip(reader.config.lineSpacing)
        }
    }

    private fun request(requestIndex: Int) {
        requester.lazyRequest(requestIndex)
                .subscribe({ text ->
                    val pages = typesetting(reader.chapterList[requestIndex].name, text)
                    pagesCache.put(requestIndex, pages)
                    debug { "request result $requestIndex == $chapterIndex" }
                    if (requestIndex == chapterIndex) {
                        pager?.refresh()
                    }
                }, {
                    //TODO 处理error,
                    val message = "小说章节获取失败：$requestIndex, ${reader.chapterList[requestIndex].name}"
                    error { message }
                })
    }

    private fun typesetting(chapter: String, text: Text): List<Page> {
        val pages = mutableListOf<Page>()
        var height = 0
        val lines = mutableListOf<Any>()
        val lineSpacing = reader.ctx.dip(reader.config.lineSpacing)
        val paragraphSpacing = reader.ctx.dip(reader.config.paragraphSpacing)
        val textHeight = textPaint.textSize.toInt()
        (listOf(chapter) + text.list).forEachIndexed { index, str ->
            val paragraph = if (index == 0) str else "　　" + str
            var start = 0
            var count: Int
            while (start < paragraph.length) {
                height += textHeight
                verbose { "typesetting height $height/${contentSize.height}" }
                if (height > contentSize.height) {
                    height = textHeight
                    debug { "add lines size ${lines.size}" }
                    pages.add(Page(ArrayList(lines)))
                    lines.clear()
                }
                count = textPaint.breakText(paragraph.substring(start), true, contentSize.width.toFloat(), null)
                val line = paragraph.substring(start, start + count)
                lines.add(line)
                height += lineSpacing
                start += count
            }
            height += paragraphSpacing
            lines.add(ParagraphSpacing(paragraphSpacing))
        }
        if (lines.isNotEmpty()) {
            debug { "add lines size ${lines.size}" }
            pages.add(Page(ArrayList(lines)))
        }
        debug { "pages size = ${pages.size}" }
        return pages
    }

    override fun scrollToPrev(): Boolean {
        val pages = pagesCache[chapterIndex]
        if (pages == null) {
            request(chapterIndex)
            return false
        }
        if (pageIndex - 1 in pages.indices) {
            pageIndex--
            return true
        }

        if (chapterIndex - 1 in reader.chapterList.indices) {
            chapterIndex--
            pageIndex = -1
            return true
        }
        return false
    }

    override fun scrollToNext(): Boolean {
        val pages = pagesCache[chapterIndex]
        if (pages == null) {
            request(chapterIndex)
            return false
        }
        if (pageIndex >= 0 && pageIndex + 1 in pages.indices) {
            pageIndex++
            return true
        }
        if (chapterIndex + 1 in reader.chapterList.indices) {
            chapterIndex++
            pageIndex = 0
            return true
        }
        return false
    }
}