package cc.aoeiuv020.panovel.text

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
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
    private val headerView = View.inflate(ctx, R.layout.novel_text_header, null).apply {
        chapterNameTextView.setTextColor(Settings.textColor)
    }
    private val textListAdapter = NovelTextListAdapter(ctx)
    private var paragraphSpacing = Settings.paragraphSpacing
    private var textProgress: Int? = null

    init {
        itemView.textListView.apply {
            addHeaderView(headerView, null, false)
            dividerHeight = ctx.dip(paragraphSpacing)
            setOnTouchListener(object : View.OnTouchListener {
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
        }
    }

    fun apply(chapter: NovelChapter) {
        itemView.progressBar.show()
        headerView.chapterNameTextView.text = chapter.name
        itemView.textListView.adapter = null
        presenter.requestNovelText(chapter)
    }

    fun showText(novelText: NovelText) {
        textListAdapter.setNovelText(novelText)
        itemView.textListView.apply {
            adapter = textListAdapter
            textProgress?.let {
                post {
                    setSelection(it)
                }
                textProgress = null
            }
        }
        itemView.progressBar.hide()
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