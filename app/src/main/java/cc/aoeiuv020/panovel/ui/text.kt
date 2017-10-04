@file:Suppress("DEPRECATION")

package cc.aoeiuv020.panovel.ui

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.SeekBar
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.DetailRequester
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.api.NovelText
import cc.aoeiuv020.panovel.local.Settings
import cc.aoeiuv020.panovel.presenter.NovelTextPresenter
import cc.aoeiuv020.panovel.ui.base.NovelTextBaseFullScreenActivity
import kotlinx.android.synthetic.main.activity_novel_text.*
import kotlinx.android.synthetic.main.novel_text_header.view.*
import kotlinx.android.synthetic.main.novel_text_item.view.*
import kotlinx.android.synthetic.main.novel_text_page_item.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.browse
import org.jetbrains.anko.debug
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

/**
 *
 * Created by AoEiuV020 on 2017.10.03-19:06:44.
 */
class NovelTextActivity : NovelTextBaseFullScreenActivity() {
    private val alertDialog: AlertDialog by lazy { AlertDialog.Builder(this).create() }
    private val progressDialog: ProgressDialog by lazy { ProgressDialog(this) }
    private lateinit var presenter: NovelTextPresenter
    private lateinit var novelName: String
    private lateinit var chaptersAsc: List<NovelChapter>
    private var index: Int by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val novelItem = intent.getSerializableExtra("novel") as NovelItem
        val requester = intent.getSerializableExtra("requester") as DetailRequester
        debug { "receive $requester" }
        novelName = novelItem.name
        index = intent.getIntExtra("index", 0)

        urlTextView.text = requester.url
        urlBar.setOnClickListener {
            browse(urlTextView.text.toString())
        }
        loading(progressDialog, R.string.novel_page)

        // 监听器确保只添加一次，
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                hide()
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                debug { "onPageSelected: $position" }
                val chapter = chaptersAsc[position]
                title = "$novelName - ${chapter.name}"
                urlTextView.text = chapter.requester.url
            }
        })

        val textSize = Settings.textSize
        debug { "load textSite = $textSize" }
        textSizeSeekBar.progress = textSize - 12
        textSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val size = 12 + progress
                (viewPager.adapter as NovelTextPagerAdapter).setTextSize(size)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val size = 12 + seekBar.progress
                Settings.textSize = size
            }

        })

        presenter = NovelTextPresenter(this, requester, index)
        presenter.start()
    }

    fun showError(message: String, e: Throwable) {
        progressDialog.dismiss()
        alertError(alertDialog, message, e)
        show()
    }

    fun showChapters(chaptersAsc: List<NovelChapter>) {
        this.chaptersAsc = chaptersAsc
        progressDialog.dismiss()
        if (chaptersAsc.isEmpty()) {
            alert(alertDialog, R.string.novel_not_support)
            // 无法浏览的情况显示状态栏标题栏导航栏，方便离开，
            show()
            return
        }
        viewPager.adapter = NovelTextPagerAdapter(this, presenter, chaptersAsc)
        viewPager.currentItem = index
    }
}

class NovelTextPagerAdapter(private val ctx: NovelTextActivity, private val presenter: NovelTextPresenter, private val chaptersAsc: List<NovelChapter>) : PagerAdapter(), AnkoLogger {
    private val unusedHolders: LinkedList<ViewHolder> = LinkedList()
    private val usedHolders: LinkedList<ViewHolder> = LinkedList()
    override fun isViewFromObject(view: View, obj: Any) = (obj as ViewHolder).view === view
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val holder = if (unusedHolders.isNotEmpty()) {
            unusedHolders.pop()
        } else {
            ViewHolder(ctx, presenter, View.inflate(ctx, R.layout.novel_text_page_item, null).apply {
                textListView.setOnItemClickListener { _, _, _, _ ->
                    (context as NovelTextActivity).toggle()
                }
            })
        }.also { usedHolders.push(it) }
        val chapter = chaptersAsc[position]
        holder.apply(chapter)
        container.addView(holder.view)
        return holder
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any?) {
        val holder = obj as ViewHolder
        val view = holder.view
        container.removeView(view)
        holder.let {
            usedHolders.remove(it)
            unusedHolders.push(holder)
        }
    }

    override fun getCount() = chaptersAsc.size

    fun setTextSize(size: Int) {
        debug { "NovelTextPagerAdapter.setTextSize $size" }
        usedHolders.forEach {
            it.setTextSize(size)
        }
        unusedHolders.forEach {
            it.setTextSize(size)
        }
    }

    class ViewHolder(private val ctx: NovelTextActivity, presenter: NovelTextPresenter, val view: View) : AnkoLogger {
        private val presenter = presenter.subPresenter(this)
        private val headerView by lazy {
            View.inflate(ctx, R.layout.novel_text_header, null)
        }
        private val textListAdapter = NovelTextListAdapter(ctx)

        init {
            view.textListView.apply {
                addHeaderView(headerView)
            }
        }

        fun apply(chapter: NovelChapter) {
            view.progressBar.show()
            headerView.chapterNameTextView.text = chapter.name
            view.textListView.adapter = null
            presenter.requestNovelText(chapter)
        }

        fun showText(novelText: NovelText) {
            textListAdapter.setNovelText(novelText)
            view.textListView.adapter = textListAdapter
            view.progressBar.hide()
        }

        fun showError(message: String, e: Throwable) {
            ctx.showError(message, e)
        }

        fun setTextSize(size: Int) {
            debug { "NovelTextPagerAdapter.ViewHolder.setTextSize $size" }
            textListAdapter.setTextSize(size)
        }
    }
}

class NovelTextListAdapter(private val ctx: Context) : BaseAdapter(), AnkoLogger {
    private var items = emptyList<String>()
    private var textSize = Settings.textSize

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View
            = (convertView ?: LayoutInflater.from(ctx).inflate(R.layout.novel_text_item, parent, false)).apply {
        textView.text = "        " + getItem(position)
        textView.textSize = textSize.toFloat()
    }

    override fun getItem(position: Int) = items[position]

    override fun getItemId(position: Int) = 0L

    override fun getCount() = items.size

    fun setTextSize(size: Int) {
        debug { "NovelTextListAdapter.setTextSize $size" }
        this.textSize = size
        notifyDataSetChanged()
    }

    fun setNovelText(novelText: NovelText) {
        debug { items.size }
        items = novelText.textList.let { if (it is RandomAccess) it else ArrayList(it) }
        notifyDataSetChanged()
    }
}
