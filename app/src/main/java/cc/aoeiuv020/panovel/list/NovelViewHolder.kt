package cc.aoeiuv020.panovel.list

import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.view.View
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.text.CheckableImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.novel_item_big.view.*
import java.text.SimpleDateFormat
import java.util.*

class NovelViewHolder(itemView: View,
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
            itemListener.requireRefresh(this)
        } else {
            // 显示是否有更新，
            refreshingDot?.refreshed(novel.receiveUpdateTime > novel.readTime)
        }
    }
}
