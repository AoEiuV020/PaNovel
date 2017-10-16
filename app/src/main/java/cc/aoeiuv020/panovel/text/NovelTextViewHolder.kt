package cc.aoeiuv020.panovel.text

import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import cc.aoeiuv020.panovel.IView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.NovelText
import cc.aoeiuv020.panovel.local.Settings
import cc.aoeiuv020.panovel.util.hide
import cc.aoeiuv020.panovel.util.show
import cn.lemon.view.RefreshRecyclerView
import kotlinx.android.synthetic.main.novel_text_header.view.*
import kotlinx.android.synthetic.main.novel_text_page_item.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug

class NovelTextViewHolder(private val ctx: NovelTextActivity, private val presenter: NovelTextPresenter.NTPresenter) : IView, AnkoLogger {
    val itemView: View = View.inflate(ctx, R.layout.novel_text_page_item, null)
    private val headerView: View
    private val chapterNameTextView: TextView
    private val textRecyclerView: RefreshRecyclerView
    private val layoutManager: LinearLayoutManager
    private val progressBar: ProgressBar
    private val textListAdapter = NovelTextRecyclerAdapter(ctx)
    private var textProgress: Int? = null

    init {
        textRecyclerView = itemView.textRecyclerView
        layoutManager = LinearLayoutManager(ctx)
        textRecyclerView.setLayoutManager(layoutManager)
        headerView = LayoutInflater.from(ctx).inflate(R.layout.novel_text_header, itemView as ViewGroup, false)
        headerView.setOnClickListener {
            ctx.toggle()
        }
        headerView.setOnLongClickListener {
            refresh()
        }
        textListAdapter.header = headerView
        textRecyclerView.setAdapter(textListAdapter)
        chapterNameTextView = headerView.chapterNameTextView
        chapterNameTextView.setTextColor(Settings.textColor)
        progressBar = itemView.progressBar
    }

    fun refresh(): Boolean {
        progressBar.show()
        presenter.refresh()
        return true
    }

    fun apply(chapter: NovelChapter) {
        progressBar.show()
        chapterNameTextView.text = chapter.name
        textListAdapter.clear()
        presenter.attach(this)
        presenter.requestNovelText(chapter)
    }

    fun destroy() {
        presenter.detach()
    }

    fun showText(novelText: NovelText) {
        textListAdapter.data = novelText.textList
        textProgress?.let {
            textRecyclerView.recyclerView.run {
                post { scrollToPosition(it) }
            }
            textProgress = null
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
        chapterNameTextView.setTextColor(color)
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