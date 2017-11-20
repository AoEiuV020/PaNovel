package cc.aoeiuv020.panovel.bookshelf

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import cc.aoeiuv020.panovel.IView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.NovelDetail
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.detail.NovelDetailActivity
import cc.aoeiuv020.panovel.local.Bookshelf
import cc.aoeiuv020.panovel.local.Text
import cc.aoeiuv020.panovel.search.RefineSearchActivity
import cc.aoeiuv020.panovel.text.NovelTextActivity
import cc.aoeiuv020.panovel.util.hide
import cc.aoeiuv020.panovel.util.show
import cn.lemon.view.adapter.BaseViewHolder
import cn.lemon.view.adapter.RecyclerAdapter
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.bookshelf_item.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.selector
import java.text.SimpleDateFormat
import java.util.*

/**
 *
 * Created by AoEiuV020 on 2017.10.14-21:54.
 */
class BookshelfAdapter(context: Context, val bookshelfPresenter: BookshelfPresenter) : RecyclerAdapter<NovelItem>(context) {
    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    override fun onCreateBaseViewHolder(parent: ViewGroup?, viewType: Int): BaseViewHolder<NovelItem>
            = ViewHolder(parent, R.layout.bookshelf_item)

    override fun onViewRecycled(holder: BaseViewHolder<NovelItem>) {
        // header和footer会强转失败，
        (holder as? ViewHolder)?.destroy()
    }

    inner class ViewHolder(parent: ViewGroup?, layoutId: Int) : BaseViewHolder<NovelItem>(parent, layoutId), IView, AnkoLogger {
        private val presenter = bookshelfPresenter.subPresenter()
        private val image = itemView.imageView
        private val name = itemView.tvName
        private val author = itemView.tvAuthor
        private val site = itemView.tvSite
        private val update = itemView.tvUpdate
        private val readAt = itemView.tvReadAt
        private val last = itemView.tvLast
        private val newChapterDot = itemView.newChapterDot
        private val progressBar = itemView.progressBar
        private val dotLayout = itemView.dotLayout
        private lateinit var novel: NovelItem

        init {
            name.setOnClickListener {
                detail()
            }

            name.setOnLongClickListener {
                refineSearch()
                true
            }

            last.setOnClickListener {
                readLastChapter()
            }

            itemView.setOnClickListener {
                readContinue()
            }

            dotLayout.setOnClickListener {
                refresh()
            }

            dotLayout.setOnLongClickListener {
                val list = listOf(R.string.read_continue to { readContinue() },
                        R.string.read_last_chapter to { readLastChapter() },
                        R.string.detail to { detail() },
                        R.string.export_exists_chapter_to_text_file to { exportExistsChapterToTextFile() },
                        R.string.refine_search to { refineSearch() },
                        R.string.refresh to { refresh() },
                        R.string.remove to { remove() })
                context.selector(context.getString(R.string.action), list.unzip().first.map { context.getString(it) }) { _, i ->
                    list[i].second.invoke()
                }
                true
            }
            itemView.ivMoreAction.show()
        }

        private fun exportExistsChapterToTextFile() {
            Text.exportExistsChapterToTextFile(novel)
        }

        private fun remove() {
            Bookshelf.remove(novel)
            bookshelfPresenter.refresh()
        }

        private fun refresh() {
            setData(novel)
            presenter.forceRefresh(novel)
        }

        private fun detail() {
            NovelDetailActivity.start(context, novel)
        }

        private fun refineSearch() {
            RefineSearchActivity.start(context, novel.name, novel.author)
        }

        private fun readLastChapter() {
            NovelTextActivity.start(context, novel, -1)
        }

        private fun readContinue() {
            NovelTextActivity.start(context, novel)
        }

        override fun setData(data: NovelItem) {
            this.novel = data

            name.text = novel.name
            author.text = novel.author
            site.text = novel.site

            newChapterDot.hide()
            progressBar.show()

            // 清空残留数据，避免闪烁，
            update.text = ""
            last.text = ""
            image.setImageDrawable(null)
            readAt.text = ""

            presenter.attach(this)
            presenter.requestDetail(novel)
        }

        fun showDetail(detail: NovelDetail) {
            showUpdateTime(detail.update)
            Glide.with(context).load(detail.bigImg).into(image)
            presenter.requestUpdate(detail)
            presenter.requestChapters(detail)
        }

        fun showUpdateTime(updateTime: Date) {
            update.text = sdf.format(updateTime)
        }

        fun showChapter(chapters: List<NovelChapter>, progress: Int) {
            readAt.text = chapters[progress].name
            last.text = chapters.last().name
            progressBar.hide()
            if (chapters.lastIndex > progress) {
                newChapterDot.show()
            }
        }

        fun destroy() {
            presenter.detach()
        }
    }
}