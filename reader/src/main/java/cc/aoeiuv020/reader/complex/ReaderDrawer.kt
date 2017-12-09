package cc.aoeiuv020.reader.complex

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
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
    val pagesCache: LruCache<Int, List<Page>?> = LruCache(8)
    private lateinit var textPaint: TextPaint
    private var backgroundImage: Bitmap? = null
    var chapterIndex = 0
    var pageIndex = 0

    init {
        reader.config.listeners.add(object : ConfigChangedListener {
            private fun refresh() {
                reset()
                pager?.refresh()
            }

            override fun onConfigChanged(name: ReaderConfigName) {
                when (name) {
                    ReaderConfigName.AnimDurationMultiply -> {
                        pager?.animDurationMultiply = reader.config.animationSpeed
                    }
                    ReaderConfigName.AnimationMode -> {
                        pager?.animMode = reader.config.animationMode.toAnimMode()
                    }
                    ReaderConfigName.BackgroundColor -> {
                        pager?.bgColor = reader.config.backgroundColor
                        refresh()
                    }
                    else -> {
                        pager?.margins = reader.config.margins
                        refresh()
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
            color = reader.config.textColor
            textSize = reader.ctx.sp(reader.config.textSize).toFloat()
        }
        backgroundImage = reader.config.backgroundImage?.let { BitmapFactory.decodeStream(reader.ctx.contentResolver.openInputStream(it)) }
    }

    override fun drawCurrentPage(background: Canvas, content: Canvas) {
        debug { "drawCurrentPage <$chapterIndex, $pageIndex>" }

        backgroundImage?.let {
            background.drawBitmap(it, null, Rect(0, 0, backgroundSize.width, backgroundSize.height), null)
        }

        if (pager == null) {
            warn { "pager is null" }
            return
        }

        if (chapterIndex !in reader.chapterList.indices) {
            warn { "chapter index out of bounds <$chapterIndex/${reader.chapterList.size}>" }
            return
        }

        val textHeight = textPaint.textSize.toInt()

        val pages = pagesCache[chapterIndex]
        if (pages == null) {
            debug { "chapter $chapterIndex pages null" }
            content.drawText("正在获取章节...", 0f, textHeight.toFloat(), textPaint)
            request(chapterIndex)
            return
        }

        if (pages.isEmpty()) {
            debug { "chapter $chapterIndex pages empty" }
            var y = textHeight
            content.drawText("本章空内容，", 0f, y.toFloat(), textPaint)
            y += textHeight
            content.drawText("网络问题？", 0f, y.toFloat(), textPaint)
            y += textHeight
            content.drawText("试试刷新？", 0f, y.toFloat(), textPaint)
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
                    y += textHeight
                    content.drawText(line, 0f, y.toFloat(), textPaint)
                    y += reader.ctx.dip(reader.config.lineSpacing)
                }
                is ParagraphSpacing -> y += paragraphSpacing
            }
        }
    }

    private val requestingList = mutableSetOf<Int>()
    private fun request(requestIndex: Int, refresh: Boolean = false) {
        if (requestingList.contains(requestIndex)) {
            // 已经在异步请求章节了，
            return
        }
        requester.lazyRequest(requestIndex, refresh)
                .subscribe({ text ->
                    val pages = typesetting(reader.chapterList[requestIndex].name, text)
                    pagesCache.put(requestIndex, pages)
                    requestingList.remove(requestIndex)
                    debug { "request result $requestIndex == $chapterIndex" }
                    if (requestIndex == chapterIndex) {
                        pager?.refresh()
                    }
                }, {
                    val message = "小说章节获取失败：$requestIndex, ${reader.chapterList[requestIndex].name}"
                    error { message }
                    // 缓存空的页面，到时候显示本章空内容，
                    pagesCache.put(requestIndex, listOf())
                    requestingList.remove(requestIndex)
                    if (requestIndex == chapterIndex) {
                        pager?.refresh()
                    }
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
        } else if (pageIndex - 1 in pages.indices) {
            pageIndex--
            return true
        }

        if (chapterIndex - 1 in reader.chapterList.indices) {
            chapterIndex--
            pageIndex = -1
            reader.chapterChangeListener?.onChapterChange()
            return true
        }
        return false
    }

    override fun scrollToNext(): Boolean {
        val pages = pagesCache[chapterIndex]
        if (pages == null) {
            request(chapterIndex)
        } else {
            if (chapterIndex + 1 in reader.chapterList.indices) {
                // 提前缓存一章，
                request(chapterIndex + 1)
            }
            if (pageIndex >= 0 && pageIndex + 1 in pages.indices) {
                pageIndex++
                return true
            }
        }
        if (chapterIndex + 1 in reader.chapterList.indices) {
            chapterIndex++
            pageIndex = 0
            reader.chapterChangeListener?.onChapterChange()
            return true
        }
        return false
    }

    fun refreshCurrentChapter() {
        pagesCache.remove(chapterIndex)
        pager?.refresh()
        request(chapterIndex, true)
    }
}