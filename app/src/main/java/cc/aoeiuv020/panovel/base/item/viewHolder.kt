package cc.aoeiuv020.panovel.base.item

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.NovelDetail
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.detail.NovelDetailActivity
import cc.aoeiuv020.panovel.local.Bookshelf
import cc.aoeiuv020.panovel.search.RefineSearchActivity
import cc.aoeiuv020.panovel.text.NovelTextActivity
import cn.lemon.view.adapter.BaseViewHolder
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.novel_item.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import java.text.SimpleDateFormat
import java.util.*

/**
 *
 * Created by AoEiuV020 on 2017.11.22-11:19:03.
 */
abstract class BaseItemViewHolder<out T : BaseItemPresenter<*>>(protected val itemListPresenter: BaseItemListPresenter<out BaseItemListView>, protected val ctx: Context, parent: ViewGroup?, layoutId: Int)
    : BaseViewHolder<NovelItem>(parent, layoutId), BaseItemView, AnkoLogger {
    companion object {
        @SuppressLint("SimpleDateFormat")
        private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    }

    @Suppress("UNCHECKED_CAST")
    protected val presenter: T = itemListPresenter.subPresenter<BaseItemViewHolder<*>>() as T
    private val image = itemView.imageView
    private val name = itemView.tvName
    private val author = itemView.tvAuthor
    private val site = itemView.tvSite
    private val update = itemView.tvUpdate
    private val readAt = itemView.tvReadAt
    private val last = itemView.tvLast
    protected lateinit var novelItem: NovelItem

    init {
        name.setOnClickListener {
            NovelDetailActivity.start(ctx, novelItem)
        }

        name.setOnLongClickListener {
            RefineSearchActivity.start(ctx, novelItem.name, novelItem.author)
            true
        }

        last.setOnClickListener {
            NovelTextActivity.start(ctx, novelItem, -1)
        }

        itemView.setOnClickListener {
            NovelTextActivity.start(ctx, novelItem)
        }
    }

    override fun setData(data: NovelItem) {
        this.novelItem = data
        debug {
            "${this.hashCode()} $layoutPosition setData $data"
        }
        @Suppress("UnnecessaryVariable")
        val novel = data
        name.text = novel.name
        author.text = novel.author
        site.text = novel.site

        // 清空残留数据，避免闪烁，
        update.text = ""
        last.text = ""
        image.setImageDrawable(null)
        readAt.text = ""

        presenter.attach(this)
        presenter.requestDetail(novel)
    }

    override fun showDetail(novelDetail: NovelDetail) {
        showUpdateTime(novelDetail.update)
        Glide.with(ctx).load(novelDetail.bigImg).into(image)
        presenter.requestUpdate(novelDetail)
        presenter.requestChapters(novelDetail)
    }

    override fun showUpdateTime(updateTime: Date) {
        update.text = sdf.format(updateTime)
    }

    override fun showChapter(chapters: List<NovelChapter>, progress: Int) {
        readAt.text = chapters[progress].name
        last.text = chapters.last().name
    }

    fun destroy() {
        presenter.detach()
    }
}

open class DefaultItemViewHolder(itemListPresenter: BaseItemListPresenter<out BaseItemListView>, ctx: Context, parent: ViewGroup?, layoutId: Int)
    : BaseItemViewHolder<BaseItemPresenter<DefaultItemViewHolder>>(itemListPresenter, ctx, parent, layoutId) {
    private val star = itemView.ivStar

    init {
        star.apply {
            setOnClickListener {
                toggle()
                if (isChecked) {
                    Bookshelf.add(novelItem)
                } else {
                    Bookshelf.remove(novelItem)
                }
            }
        }
    }

    override fun setData(data: NovelItem) {
        super.setData(data)
        star.isChecked = Bookshelf.contains(novelItem)
    }
}

