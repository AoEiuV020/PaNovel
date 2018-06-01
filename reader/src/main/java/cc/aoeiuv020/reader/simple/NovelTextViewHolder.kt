package cc.aoeiuv020.reader.simple

import android.app.Activity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import cc.aoeiuv020.reader.R
import cc.aoeiuv020.reader.hide
import cc.aoeiuv020.reader.show
import kotlinx.android.synthetic.main.simple_view_pager_item.view.*
import org.jetbrains.anko.*
import kotlin.properties.Delegates

internal class NovelTextViewHolder(private val reader: SimpleReader) : AnkoLogger {
    // TODO 这个强转不能留，
    private val ctx = reader.ctx as Activity
    private val requester = reader.requester
    val itemView: View = View.inflate(ctx, R.layout.simple_view_pager_item, null)
    var position: Int = 0
    private val textRecyclerView = itemView.textRecyclerView
    private val layoutManager: LinearLayoutManager = LinearLayoutManager(ctx)
    private val progressBar: ProgressBar = itemView.progressBar
    val ntrAdapter = NovelTextRecyclerAdapter(reader)
    private var textProgress: Int? = null
    private var index: Int by Delegates.notNull()

    init {
        textRecyclerView.layoutManager = layoutManager
        textRecyclerView.adapter = ntrAdapter
        // itemView可能没有初始化高度，所以用decorView,
        // 更靠谱的是GlobalOnLayoutListener，但要求api >= 16,
        textRecyclerView.apply {
            layoutParams = (layoutParams as ViewGroup.MarginLayoutParams).apply {
                setMargins(leftMargin,
                        reader.config.contentMargins.top.run { (toFloat() / 100 * ctx.window.decorView.height).toInt() },
                        rightMargin,
                        reader.config.contentMargins.bottom.run { (toFloat() / 100 * ctx.window.decorView.height).toInt() })
            }
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                    reader.readingListener?.onReading(reader.currentChapter, getTextProgress())
                }
            })
        }
    }

    fun request(index: Int, refresh: Boolean = false) {
        this.index = index
        val chapter = reader.chapterList[index]
        progressBar.show()
        ntrAdapter.clear()
        ntrAdapter.setChapterName(chapter)
        // TODO: 重复刷新要忽略先刷新的，但不能直接中断线程，可能导致数据异常，
        doAsync({ e ->
            val message = "获取小说文本失败，"
            error(message, e)
            showError(message, e)
        }) {
            val novelText = requester.request(index, refresh)
            uiThread {
                showText(novelText)
            }
        }
    }

    fun destroy() {
    }

    fun refresh() {
        request(index, true)
    }

    private fun showText(text: List<String>) {
        ntrAdapter.data = text
        textProgress?.let {
            textRecyclerView.run {
                post { scrollToPosition(it) }
            }
            textProgress = null
        }
        progressBar.hide()
    }

    @Suppress("UNUSED_PARAMETER")
    private fun showError(message: String, e: Throwable) {
        itemView.progressBar.hide()
    }

    fun notifyMarginsChanged() {
        textRecyclerView.apply {
            post {
                layoutParams = (layoutParams as ViewGroup.MarginLayoutParams).apply {
                    setMargins(leftMargin,
                            reader.config.contentMargins.top.run { (toFloat() / 100 * itemView.height).toInt() },
                            rightMargin,
                            reader.config.contentMargins.bottom.run { (toFloat() / 100 * itemView.height).toInt() })
                }
            }
        }
    }

    fun setTextProgress(textProgress: Int) {
        debug { "setTextProgress $textProgress" }
        // 存起来，recyclerView可能还没得到数据，
        this.textProgress = textProgress
        textRecyclerView.scrollToPosition(textProgress)
    }

    fun getTextProgress(): Int {
        return layoutManager.findLastVisibleItemPosition().also {
            debug { "getTextProgress $it" }
        }
    }

    fun getTextCount(): Int = ntrAdapter.itemCount
}