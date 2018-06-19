package cc.aoeiuv020.panovel.list

import android.content.Context
import android.support.annotation.UiThread
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.bookshelf.RefreshingDotView
import cc.aoeiuv020.panovel.data.NovelManager
import cc.aoeiuv020.panovel.settings.ItemAction
import cc.aoeiuv020.panovel.settings.ListSettings
import cc.aoeiuv020.panovel.text.CheckableImageView
import cc.aoeiuv020.panovel.util.noCover
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.novel_item_big.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.dip
import java.util.*
import java.util.concurrent.TimeUnit


class NovelViewHolder(itemView: View,
                      dotColor: Int,
                      dotSize: Float,
                      private val refreshingNovelSet: MutableSet<Long>,
                      private val shouldRefreshSet: MutableSet<Long>,
                      initItem: (NovelViewHolder) -> Unit = {},
                      actionDoneListener: (ItemAction, NovelViewHolder) -> Unit = { _, _ -> },
                      onError: (String, Throwable) -> Unit
) : RecyclerView.ViewHolder(itemView), AnkoLogger {
    private val itemListener = DefaultNovelItemActionListener(actionDoneListener, onError)

    // 所有View可空，准备支持不同布局，小的布局可能大部分View都没有，
    val name: TextView? = itemView.tvName
    val author: TextView? = itemView.tvAuthor
    val site: TextView? = itemView.tvSite
    val image: ImageView? = itemView.ivImage
    val last: TextView? = itemView.tvLast
    val checkUpdate: TextView? = itemView.tvCheckUpdate
    val readAt: TextView? = itemView.tvReadAt
    val star: CheckableImageView? = itemView.ivStar
    val refreshingDot: RefreshingDotView? = itemView.rdRefreshing
    // 包括刷新小红点和加入书架的爱心的FrameLayout,
    val flDot: View? = itemView.flDot
    // 提供外面的加调方法使用，
    lateinit var novelManager: NovelManager
        private set
    val novel get() = novelManager.novel
    val ctx: Context = itemView.context

    init {
        // 这里的引用的设置修改后不会马上生效，因为ViewHolder会被复用，
        // 无所谓了，要是从外面传进来的话就太烦了，

        val typedValue = TypedValue()
        ctx.theme.resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true)
        // 长按时波纹背景，
        val selectableItemBackground = typedValue.resourceId

        if (ListSettings.onDotClick != ItemAction.None) {
            refreshingDot?.setOnClickListener {
                itemListener.onDotClick(this)
            }
            refreshingDot?.setBackgroundResource(selectableItemBackground)
        }

        if (ListSettings.onDotLongClick != ItemAction.None) {
            refreshingDot?.setOnLongClickListener {
                itemListener.onDotLongClick(this)
            }
            refreshingDot?.setBackgroundResource(selectableItemBackground)
        }

        if (ListSettings.onCheckUpdateClick != ItemAction.None) {
            checkUpdate?.setOnClickListener {
                itemListener.onCheckUpdateClick(this)
            }
            checkUpdate?.setBackgroundResource(selectableItemBackground)
        }

        if (ListSettings.onNameClick != ItemAction.None) {
            name?.setOnClickListener {
                itemListener.onNameClick(this)
            }
            // 格子视图小说名的背景是渐变黑，不能改成波纹，
        }

        if (ListSettings.onNameLongClick != ItemAction.None) {
            name?.setOnLongClickListener {
                itemListener.onNameLongClick(this)
            }
            // 格子视图小说名的背景是渐变黑，不能改成波纹，
        }

        if (ListSettings.onLastChapterClick != ItemAction.None) {
            last?.setOnClickListener {
                itemListener.onLastChapterClick(this)
            }
            last?.setBackgroundResource(selectableItemBackground)
        }

        if (ListSettings.onItemClick != ItemAction.None) {
            itemView.setOnClickListener {
                itemListener.onItemClick(this)
            }
            itemView.setBackgroundResource(selectableItemBackground)
        }

        if (ListSettings.onItemLongClick != ItemAction.None) {
            itemView.setOnLongClickListener {
                itemListener.onItemLongClick(this)
            }
            itemView.setBackgroundResource(selectableItemBackground)
        }

        // TODO: star控件改成支持onCheckChanged，这样的话，要试试外部调用移出书架指定isChecked会不会调用click事件，
        star?.setOnClickListener {
            it as CheckableImageView
            it.toggle()
            itemListener.onStarChanged(this, it.isChecked)
        }
        refreshingDot?.setDotColor(dotColor)
        refreshingDot?.setDotSize(ctx.dip(dotSize))

        initItem(this)
    }

    fun apply(novelManager: NovelManager, refreshTime: Date) {
        debug { "apply <${novelManager.novel.run { "$site.$author.$name.$checkUpdateTime" }}>, refreshTime = $refreshTime" }
        show(novelManager)

        when {
        // 比如询问服务器告知该小说有更新，就在这里刷新，
            shouldRefreshSet.remove(novel.nId) -> refresh()
            refreshingNovelSet.contains(novel.nId) -> refreshing()
        // 手动刷新后需要联网更新，
            refreshTime > novel.checkUpdateTime -> refresh()
        // 不能什么都不做，要调用refreshingDot?.refreshed明确隐藏进度条，
            else -> refreshed(novelManager)
        }
    }

    private fun show(novelManager: NovelManager) {
        this.novelManager = novelManager
        name?.text = novel.name
        author?.text = novel.author
        site?.text = novel.site
        last?.text = novel.lastChapterName
        image?.let { imageView ->
            if (novel.image == noCover) {
                imageView.setImageResource(R.mipmap.no_cover)
            } else {
                Glide.with(ctx.applicationContext)
                        .load(novelManager.getCoverImage())
                        .apply(RequestOptions().apply {
                            placeholder(R.mipmap.no_cover)
                            error(R.mipmap.no_cover)
                        })
                        .into(imageView)
            }
        }
        star?.isChecked = novel.bookshelf
        // 显示“x分钟前”，
        // TODO: 本地小说不显示这个，
        checkUpdate?.text = DateUtils.getRelativeTimeSpanString(novel.checkUpdateTime.time, System.currentTimeMillis(), TimeUnit.SECONDS.toMillis(1))
        readAt?.text = novel.readAtChapterName
        val hasNew = if (ListSettings.dotNotifyUpdate) {
            // 判断上次刷出更新时间在阅读时间之后，
            this.novel.receiveUpdateTime > this.novel.readTime
        } else {
            // 判断阅读进度章节小于最后一章，
            this.novel.chaptersCount - 1 > this.novel.readAtChapterIndex
        }
        debug {
            "show hasNew: $hasNew"
        }
        refreshingDot?.refreshed(hasNew)
    }

    @UiThread
    private fun refreshing() {
        debug { "refreshing ${name?.text}" }
        // 显示正在刷新，
        refreshingDot?.refreshing()
    }

    /**
     * 主动刷新，
     * 可以在itemListener里调用以刷新这本小说，
     */
    @UiThread
    fun refresh() {
        debug { "refresh ${name?.text}" }
        refreshing()
        refreshingNovelSet.add(novel.nId)
        itemListener.refreshChapters(this)
    }

    /**
     * 刷新结束时调用，
     */
    @UiThread
    fun refreshed(novelManager: NovelManager) {
        val novel = novelManager.novel
        debug { "refreshed <${novel.run { "$site.$author.$name" }}>" }
        debug {
            "bind <${this.novel.run { "$site.$author.$name" }}>"
        }
        refreshingNovelSet.remove(novel.nId)
        if (novel.nId == this.novel.nId) {
            // 显示刷新结果，
            show(novelManager)
        }
    }

    /**
     * 外部调用，小说移出书架，
     */
    fun removeBookshelf() {
        star?.isChecked = false
        itemListener.onStarChanged(this, false)
    }

    fun addBookshelf() {
        star?.isChecked = true
        itemListener.onStarChanged(this, true)
    }
}
