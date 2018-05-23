package cc.aoeiuv020.panovel.bookshelf

import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.text.CheckableImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.novel_item_big.view.*
import org.jetbrains.anko.doAsync
import java.text.SimpleDateFormat
import java.util.*

/**
 *
 * Created by AoEiuV020 on 2017.10.14-21:54.
 */

class BookshelfItemListAdapter
    : RecyclerView.Adapter<BookshelfItemViewHolder>() {
    var data: List<Novel> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    // TODO:
    val itemListener = NovelItemActionAdapter()

    // 打开时存个最小时间，手动刷新时更新这个时间，
    // 如果小说刷新时间checkUpdateTime小于这个时间就联网刷新章节列表，
    var refreshTime = Date(0)

    fun refresh() {
        refreshTime = Date()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookshelfItemViewHolder {
        // TODO: 不只big,
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.novel_item_big, parent, false)
        return BookshelfItemViewHolder(itemView, itemListener)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: BookshelfItemViewHolder, position: Int) {
        val novel = data[position]
        holder.apply(novel, refreshTime)
    }
}

interface NovelItemActionListener {
    fun onDotClick(vh: BookshelfItemViewHolder)
    fun onDotLongClick(vh: BookshelfItemViewHolder): Boolean
    fun onNameClick(vh: BookshelfItemViewHolder)
    fun onNameLongClick(vh: BookshelfItemViewHolder): Boolean
    fun onLastChapterClick(vh: BookshelfItemViewHolder)
    fun onItemClick(vh: BookshelfItemViewHolder)
    fun onItemLongClick(vh: BookshelfItemViewHolder): Boolean
    fun onStarChanged(vh: BookshelfItemViewHolder, star: Boolean)
}

open class NovelItemActionAdapter : NovelItemActionListener {
    override fun onDotClick(vh: BookshelfItemViewHolder) {}
    override fun onDotLongClick(vh: BookshelfItemViewHolder): Boolean = false
    override fun onNameClick(vh: BookshelfItemViewHolder) {}
    override fun onNameLongClick(vh: BookshelfItemViewHolder): Boolean = false
    override fun onLastChapterClick(vh: BookshelfItemViewHolder) {}
    override fun onItemClick(vh: BookshelfItemViewHolder) {}
    override fun onItemLongClick(vh: BookshelfItemViewHolder): Boolean = false
    override fun onStarChanged(vh: BookshelfItemViewHolder, star: Boolean) {
        vh.novel.bookshelf = true
        doAsync({ e ->
            // TODO: 想办法把这个异常展示出来，
            Reporter.postException(IllegalStateException("更新书架失败，", e))
        }) {
            DataManager.updateBookshelf(vh.novel, star)
        }
    }
}

class BookshelfItemViewHolder(itemView: View,
                              private val itemListener: NovelItemActionListener)
    : RecyclerView.ViewHolder(itemView) {
    companion object {
        // 用于格式化时间，可能有展示更新时间，
        private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    }

    // 所有View可空，准备支持不同布局，小的布局可能大部分View都没有，
    private val name = itemView.tvName
    private val author = itemView.tvAuthor
    private val site = itemView.tvSite
    private val image = itemView.imageView
    private val last = itemView.tvLast
    private val checkUpdate = itemView.tvUpdate
    private val readAt = itemView.tvReadAt
    private val star = itemView.ivStar
    private val refreshingDot = itemView.rdRefreshing
    // 提供外面的加调方法使用，
    lateinit var novel: Novel
        private set

    init {
        refreshingDot.setOnClickListener {
            itemListener.onDotClick(this)
        }

        refreshingDot.setOnLongClickListener {
            itemListener.onDotLongClick(this)
        }
        name.setOnClickListener {
            itemListener.onNameClick(this)
        }

        name.setOnLongClickListener {
            itemListener.onNameLongClick(this)
        }

        last.setOnClickListener {
            itemListener.onLastChapterClick(this)
        }

        itemView.setOnClickListener {
            itemListener.onItemClick(this)
        }

        itemView.setOnLongClickListener {
            itemListener.onItemLongClick(this)
        }

        star.setOnClickListener {
            it as CheckableImageView
            it.toggle()
            itemListener.onStarChanged(this, it.isChecked)
        }

/*
TODO:
        newChapterDot.setHeight(ctx.dip(Settings.bookshelfRedDotSize))
        newChapterDot.setColorFilter(Settings.bookshelfRedDotColor)
*/
    }

    fun apply(novel: Novel, refreshTime: Date) {
        this.novel = novel
        name?.text = novel.name
        author?.text = novel.author
        site?.text = novel.site
        last?.text = novel.lastChapterName
        image?.let { imageView ->
            Glide.with(imageView)
                    .load(novel.image)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                            // TODO: 考虑在特定某些异常出现时直接改数据库里的小说图片地址，
                            novel.image = "https://www.snwx8.com/modules/article/images/nocover.jpg"
                            Glide.with(imageView).load(novel.image)
                                    .into(target)
                            return true
                        }

                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            return false
                        }
                    })
                    .into(imageView)
        }
        star?.isChecked = novel.bookshelf
        checkUpdate?.text = sdf.format(novel.checkUpdateTime)
        readAt?.text = novel.readAtChapterName
        if (refreshTime > novel.checkUpdateTime) {
            // 手动刷新后需要联网更新，
            refreshingDot?.refreshing()
        } else {
            // 显示是否有更新，
            refreshingDot?.refreshed(novel.receiveUpdateTime > novel.readTime)
        }
    }
}
