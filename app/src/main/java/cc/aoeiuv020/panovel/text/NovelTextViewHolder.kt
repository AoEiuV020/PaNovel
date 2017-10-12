package cc.aoeiuv020.panovel.text

import android.annotation.SuppressLint
import android.support.v7.widget.LinearLayoutManager
import android.view.MotionEvent
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import cc.aoeiuv020.panovel.IView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.NovelText
import cc.aoeiuv020.panovel.local.Settings
import cc.aoeiuv020.panovel.util.hide
import cc.aoeiuv020.panovel.util.show
import com.aspsine.irecyclerview.IRecyclerView
import kotlinx.android.synthetic.main.novel_text_header.view.*
import kotlinx.android.synthetic.main.novel_text_page_item.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug

class NovelTextViewHolder(private val ctx: NovelTextActivity, presenter: NovelTextPresenter) : IView, AnkoLogger {
    val itemView: View = View.inflate(ctx, R.layout.novel_text_page_item, null)
    private val presenter = presenter.subPresenter(this)
    private val headerView = View.inflate(ctx, R.layout.novel_text_header, null)
    private val chapterNameTextView: TextView
    private val textRecyclerView: IRecyclerView
    private val layoutManager: LinearLayoutManager
    private val progressBar: ProgressBar
    private val textListAdapter = NovelTextRecyclerAdapter(ctx)
    private var textProgress: Int? = null

    init {
        textRecyclerView = itemView.textRecyclerView
        layoutManager = LinearLayoutManager(ctx)
        textRecyclerView.layoutManager = layoutManager
        textRecyclerView.iAdapter = textListAdapter
        // 这有个警告禁用不了，只能这样无意义的强转一下，
        // Custom view `ListView` has setOnTouchListener called on it but does not override performClick
        (textRecyclerView as View).setOnTouchListener(object : View.OnTouchListener {
            private var previousAction: Int = MotionEvent.ACTION_UP
            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                if (previousAction == MotionEvent.ACTION_DOWN
                        && event.action == MotionEvent.ACTION_UP) {
                    ctx.toggle()
                }
                previousAction = event.action
                return false
            }
        })
        textRecyclerView.addHeaderView(headerView)
        chapterNameTextView = headerView.chapterNameTextView
        chapterNameTextView.setTextColor(Settings.textColor)
        progressBar = itemView.progressBar
    }

    fun apply(chapter: NovelChapter) {
        progressBar.show()
        headerView.chapterNameTextView.text = chapter.name
        textListAdapter.clear()
        presenter.requestNovelText(chapter)
    }

    fun showText(novelText: NovelText) {
        textListAdapter.setNovelText(novelText)
        textRecyclerView.apply {
            textProgress?.let {
                post { scrollToPosition(it) }
                textProgress = null
            }
        }
        progressBar.hide()
    }

    fun showError(message: String, e: Throwable) {
        itemView.progressBar.hide()
        ctx.showError(message, e)
    }

    fun setTextSize(size: Int) {
        textListAdapter.setTextSize(size)
    }

    fun setLineSpacing(size: Int) {
        textListAdapter.setLineSpacing(size)
    }

    fun setParagraphSpacing(size: Int) {
        textListAdapter.setParagraphSpacing(size)
    }

    fun setTextColor(color: Int) {
        headerView.chapterNameTextView.setTextColor(color)
        textListAdapter.setTextColor(color)
    }

    fun setTextProgress(textProgress: Int) {
        debug { "setTextProgress $textProgress" }
        this.textProgress = textProgress
    }

    fun getTextProgress(): Int? {
        return layoutManager.findLastVisibleItemPosition().also {
            debug { "getTextProgress $it" }
        }
    }
}