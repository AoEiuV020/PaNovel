package cc.aoeiuv020.reader.complex

import android.content.Intent
import android.content.IntentFilter
import android.graphics.*
import android.support.v4.util.LruCache
import android.text.TextPaint
import cc.aoeiuv020.pager.IMargins
import cc.aoeiuv020.pager.Pager
import cc.aoeiuv020.pager.PagerDrawer
import cc.aoeiuv020.pager.Size
import cc.aoeiuv020.reader.ConfigChangedListener
import cc.aoeiuv020.reader.Novel
import cc.aoeiuv020.reader.ReaderConfigName
import cc.aoeiuv020.reader.ReaderConfigName.*
import cc.aoeiuv020.reader.TextRequester
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.*
import java.text.SimpleDateFormat
import java.util.*

/**
 *
 * Created by AoEiuV020 on 2017.12.03-04:09:17.
 */
@SuppressWarnings("SimpleDateFormat")
class ReaderDrawer(private val reader: ComplexReader, private val novel: Novel, private val requester: TextRequester)
    : PagerDrawer(), AnkoLogger {
    val pagesCache: LruCache<Int, List<Page>?> = LruCache(8)
    private lateinit var titlePaint: TextPaint
    private lateinit var textPaint: TextPaint
    private lateinit var messagePaint: TextPaint
    private var backgroundImage: Bitmap? = null
    var chapterIndex = 0
    var pageIndex = 0
    private var sdf = SimpleDateFormat(reader.config.dateFormat)

    init {
        reader.config.listeners.add(object : ConfigChangedListener {
            private fun refresh() {
                reset()
                pager?.refresh()
            }

            override fun onConfigChanged(name: ReaderConfigName) {
                when (name) {
                    CenterPercent -> {
                        pager?.centerPercent = reader.config.centerPercent
                    }
                    Font -> {
                        textPaint.typeface = reader.config.font
                        titlePaint.typeface = reader.config.titleFont
                    }
                    AnimDurationMultiply -> {
                        pager?.animDurationMultiply = reader.config.animationSpeed
                    }
                    ReaderConfigName.AnimationMode -> {
                        pager?.animMode = reader.config.animationMode.toAnimMode()
                    }
                    BackgroundColor -> {
                        pager?.bgColor = reader.config.backgroundColor
                    }
                    ContentMargins -> {
                        pager?.margins = reader.config.contentMargins
                    }
                    DateFormat -> {
                        // 这个不支持在阅读时改，到不了这里，
                        sdf = SimpleDateFormat(reader.config.dateFormat)
                    }
                    else -> {
                        // 其他设置在refresh里都重置了，
                    }
                }
                refresh()
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
            isAntiAlias = true
            color = reader.config.textColor
            textSize = reader.ctx.sp(reader.config.textSize).toFloat()
            typeface = reader.config.font
        }
        titlePaint = TextPaint(textPaint).apply {
            typeface = reader.config.titleFont
        }
        backgroundImage = reader.config.backgroundImage?.let { BitmapFactory.decodeStream(reader.ctx.contentResolver.openInputStream(it)) }
        messagePaint = TextPaint(textPaint).apply {
            textSize = reader.ctx.sp(reader.config.messageSize).toFloat()
        }
    }

    override fun drawCurrentPage(background: Canvas, content: Canvas) {
        debug { "drawCurrentPage <$chapterIndex, $pageIndex>" }

        drawBackground(background)

        if (pager == null) {
            warn { "pager is null" }
            return
        }

        if (chapterIndex !in reader.chapterList.indices) {
            // TODO: 打开小说时必到这里两次，
            warn { "chapter index out of bounds <$chapterIndex/${reader.chapterList.size}>" }
            return
        }

        val pages = pagesCache[chapterIndex]

        val page = initPages(content, pages)

        if (reader.config.paginationMargins.enabled) {
            drawPagination(background, pages)
        }
        if (reader.config.chapterNameMargins.enabled) {
            drawMessage(background, reader.chapterList[chapterIndex].name, reader.config.chapterNameMargins)
        }
        if (reader.config.bookNameMargins.enabled) {
            drawMessage(background, novel.name, reader.config.bookNameMargins)
        }
        if (reader.config.timeMargins.enabled) {
            drawTime(background)
        }
        if (reader.config.batteryMargins.enabled) {
            drawBattery(background)
        }

        page?.let {
            drawContent(content, page)
        }
    }

    private fun initPages(content: Canvas, pages: List<Page>?): Page? {
        // 重置自动刷新线程，
        reader.autoRefreshThread.reset()
        // 只用本地变量，防止pageIndex被多线程修改，
        var index = pageIndex
        val textHeight = textPaint.textSize.toInt()
        if (pages == null) {
            debug { "chapter $chapterIndex pages null" }
            content.drawText("正在获取章节...", 0f, textHeight.toFloat(), textPaint)
            request(chapterIndex)
            return null
        }
        if (pages.isEmpty()) {
            debug { "chapter $chapterIndex pages empty" }
            var y = textHeight
            content.drawText("本章空内容，", 0f, y.toFloat(), textPaint)
            y += textHeight
            content.drawText("网络问题？", 0f, y.toFloat(), textPaint)
            y += textHeight
            content.drawText("试试刷新？", 0f, y.toFloat(), textPaint)
            return null
        }

        // 下面两个判断和上面重复，主要是没加锁，重复判断避免万一，
        // 调小字体有可能出现页数变少，
        if (index > pages.lastIndex) {
            index = pages.lastIndex
        }
        // 往上翻章节时pageIndex会是负数，表示倒数，
        // 虽然没必要，但是尽量让pageIndex无论什么数都加上pages.size的整数倍到pages范围内，
/*
        while (pageIndex < 0) {
            pageIndex += pages.size
        }
*/
        if (index < 0) {
            index -= index / pages.size * pages.size
        }
        if (index < 0) {
            index += pages.size
        }

        pageIndex = index
        return pages[index]
    }


    private fun drawTime(canvas: Canvas) {
        val text = sdf.format(Date())
        val margins = reader.config.timeMargins
        drawMessage(canvas, text, margins)
    }

    private fun drawBattery(canvas: Canvas) {
        val intent = reader.ctx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val battery = intent.getIntExtra("level", 0)
        val text = "$battery"
        val margins = reader.config.batteryMargins
        drawMessage(canvas, text, margins, true)
    }

    private fun drawPagination(canvas: Canvas, pages: List<Page>?) {
        val max = pages?.size ?: 0
        // 负数表示倒数，保留，不+1,
        val index = if (pageIndex >= 0) pageIndex + 1 else pageIndex
        val margins = reader.config.paginationMargins
        val text = "$index/$max"
        drawMessage(canvas, text, margins)
    }

    /**
     * 左右取大的，上下取大的，小的无视，
     * 哪个大就贴哪个，
     * 刚好50是居中，
     */
    private fun drawMessage(canvas: Canvas, text: String, margins: IMargins, isBattery: Boolean = false) {
        val textHeight = messagePaint.textSize
        val textWidth = messagePaint.measureText(text)
        val x: Float = if (margins.left > margins.right) {
            if (margins.left == 50) {
                canvas.width / 2 - textWidth / 2
            } else {
                canvas.width * margins.left / 100f
            }
        } else {
            if (margins.right == 50) {
                canvas.width / 2 - textWidth / 2
            } else {
                canvas.width - canvas.width * margins.right / 100f - textWidth
            }
        }
        val y: Float = if (margins.top > margins.bottom) {
            if (margins.top == 50) {
                canvas.height / 2 + textHeight / 2
            } else {
                canvas.height * margins.top / 100f + textHeight
            }
        } else {
            if (margins.bottom == 50) {
                canvas.height / 2 + textHeight / 2
            } else {
                canvas.height - canvas.height * margins.bottom / 100f
            }
        }
        if (isBattery) {
            // 画框框，
            messagePaint.style = Paint.Style.STROKE
            messagePaint.strokeWidth = textHeight / 20
            canvas.drawRect(RectF(x, y - textHeight, x + textWidth, y), messagePaint)
            // 画电池头部那个小点，
            messagePaint.style = Paint.Style.FILL
            canvas.drawRect(RectF(x + textWidth, y - textHeight / 4 * 3, x + textWidth + textWidth / 15, y - textHeight / 4 * 1), messagePaint)

            val a = 0.1f
            val bX = x + textWidth * a
            val bY = y - textHeight * a
            val mSize = messagePaint.textSize
            val bSize = mSize * (1 - 2 * a)
            messagePaint.textSize = bSize
            canvas.drawText(text, bX, bY, messagePaint)
            messagePaint.textSize = mSize
        } else {
            canvas.drawText(text, x, y, messagePaint)
        }
    }

    private fun drawContent(content: Canvas, page: Page) {
        val textHeight = textPaint.textSize.toInt()

        var y = 0
        val paragraphSpacing = reader.ctx.dip(reader.config.paragraphSpacing)
        page.lines.forEach { line ->
            verbose { "draw height $y/${content.height}" }
            when (line) {
                is Title -> {
                    y += textHeight
                    content.drawText(line.string, 0f, y.toFloat(), titlePaint)
                    y += reader.ctx.dip(reader.config.lineSpacing)
                }
                is String -> {
                    y += textHeight
                    content.drawText(line, 0f, y.toFloat(), textPaint)
                    y += reader.ctx.dip(reader.config.lineSpacing)
                }
                is ParagraphSpacing -> y += paragraphSpacing
            }
        }
    }

    private fun drawBackground(background: Canvas) {
        backgroundImage?.let {
            background.drawBitmap(it, null, Rect(0, 0, backgroundSize.width, backgroundSize.height), null)
        }
    }

    private val requestingList = mutableSetOf<Int>()
    private fun request(requestIndex: Int, refresh: Boolean = false) {
        if (requestingList.contains(requestIndex)) {
            // 已经在异步请求章节了，
            return
        }
        requestingList.add(requestIndex)
        debug { "$this lazyRequest $requestIndex, refresh = $refresh" }
        // TODO: 不要RxJava的东西，
        Single.fromCallable {
            val text = requester.request(requestIndex, refresh)
            val pages = typesetting(reader.chapterList[requestIndex].name, text)
            pagesCache.put(requestIndex, pages)
            requestingList.remove(requestIndex)
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
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

    private fun typesetting(chapter: String, list: List<String>): List<Page> {
        val pages = mutableListOf<Page>()
        var height = 0
        val lines = mutableListOf<Any>()
        val lineSpacing = reader.ctx.dip(reader.config.lineSpacing)
        val paragraphSpacing = reader.ctx.dip(reader.config.paragraphSpacing)
        val textHeight = textPaint.textSize.toInt()
        (listOf(chapter) + list).forEachIndexed { index, str ->
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
                if (index == 0) {
                    lines.add(Title(line))
                } else {
                    lines.add(line)
                }
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
        if (pages != null && pageIndex < 0) {
            // 负数转正，如果pages.size为0也无所谓了，
            pageIndex = pages.lastIndex
        }
        val prevPageIndex = pageIndex - 1
        if (pages != null && prevPageIndex in pages.indices) {
            pageIndex--
            return true
        }
        val prevChapterIndex = chapterIndex - 1
        if (prevChapterIndex in reader.chapterList.indices) {
            chapterIndex--
            pageIndex = -1
            reader.chapterChangeListener?.onChapterChange()
            return true
        }
        return false
    }

    override fun scrollToNext(): Boolean {
        val pages = pagesCache[chapterIndex]
        val nextPageIndex = pageIndex + 1
        val nextChapterIndex = chapterIndex + 1
        if (pages != null && pageIndex >= 0 && nextPageIndex in pages.indices) {
            pageIndex++
            if (nextChapterIndex in reader.chapterList.indices && pagesCache.get(nextChapterIndex) == null) {
                // 提前缓存一章，
                request(nextChapterIndex)
            }
            return true
        }
        if (nextChapterIndex in reader.chapterList.indices) {
            chapterIndex++
            pageIndex = 0
            reader.chapterChangeListener?.onChapterChange()
            return true
        }
        return false
    }

    fun refreshCurrentChapter() {
        pagesCache.remove(chapterIndex)
        request(chapterIndex, true)
    }
}