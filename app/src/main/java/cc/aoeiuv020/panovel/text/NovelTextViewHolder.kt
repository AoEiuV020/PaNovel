package cc.aoeiuv020.panovel.text

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import cc.aoeiuv020.panovel.IView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.NovelText
import cc.aoeiuv020.panovel.local.Settings
import cc.aoeiuv020.panovel.util.hide
import cc.aoeiuv020.panovel.util.show
import kotlinx.android.synthetic.main.novel_text_header.view.*
import kotlinx.android.synthetic.main.novel_text_page_item.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.dip

class NovelTextViewHolder(private val ctx: NovelTextActivity, presenter: NovelTextPresenter) : IView, AnkoLogger {
    val itemView: View = View.inflate(ctx, R.layout.novel_text_page_item, null)
    private val presenter = presenter.subPresenter(this)
    private val headerView = View.inflate(ctx, R.layout.novel_text_header, null)
    private val chapterNameTextView: TextView
    private val textListView: ListView
    private val progressBar: ProgressBar

    private val textListAdapter = NovelTextListAdapter(ctx)
    private var paragraphSpacing = Settings.paragraphSpacing
    private var textProgress: Int? = null

    init {
        textListView = itemView.textListView
        textListView.addHeaderView(headerView, null, false)
        textListView.dividerHeight = ctx.dip(paragraphSpacing)
        // 这有个警告禁用不了，只能这样无意义的强转一下，
        // Custom view `ListView` has setOnTouchListener called on it but does not override performClick
        (textListView as View).setOnTouchListener(object : View.OnTouchListener {
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
        chapterNameTextView = headerView.chapterNameTextView
        chapterNameTextView.setTextColor(Settings.textColor)
        progressBar = itemView.progressBar
    }

    fun apply(chapter: NovelChapter) {
        progressBar.show()
        headerView.chapterNameTextView.text = chapter.name
        textListView.adapter = null
        presenter.requestNovelText(chapter)
    }

    fun showText(novelText: NovelText) {
        textListAdapter.setNovelText(novelText)
        textListView.apply {
            adapter = textListAdapter
            textProgress?.let {
                post { setSelection(it) }
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
        itemView.textListView.dividerHeight = ctx.dip(size)
    }

    fun setTextColor(color: Int) {
        headerView.chapterNameTextView.setTextColor(color)
        textListAdapter.setTextColor(color)
    }

    fun setTextProgress(textProgress: Int) {
        this.textProgress = textProgress
    }
}