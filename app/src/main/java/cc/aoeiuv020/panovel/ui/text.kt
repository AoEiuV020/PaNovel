@file:Suppress("DEPRECATION")

package cc.aoeiuv020.panovel.ui

import android.annotation.SuppressLint
import android.app.Activity
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
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.NovelListItem
import cc.aoeiuv020.panovel.api.NovelText
import cc.aoeiuv020.panovel.local.Settings
import cc.aoeiuv020.panovel.presenter.NovelTextPresenter
import cc.aoeiuv020.panovel.ui.base.NovelTextBaseFullScreenActivity
import kotlinx.android.synthetic.main.activity_novel_text.*
import kotlinx.android.synthetic.main.novel_text_item.view.*
import kotlinx.android.synthetic.main.novel_text_item_loading.view.*
import kotlinx.android.synthetic.main.novel_text_page_item.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.browse
import org.jetbrains.anko.debug

/**
 *
 * Created by AoEiuV020 on 2017.10.03-19:06:44.
 */
class NovelTextActivity : NovelTextBaseFullScreenActivity() {
    private val alertDialog: AlertDialog by lazy { AlertDialog.Builder(this).create() }
    private val progressDialog: ProgressDialog by lazy { ProgressDialog(this) }
    private lateinit var presenter: NovelTextPresenter
    private lateinit var novelName: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val novelListItem = intent.getSerializableExtra("item") as NovelListItem
        debug { "receive $novelListItem" }
        novelName = novelListItem.novel.name
        val index = intent.getIntExtra("index", 0)

        urlTextView.text = novelListItem.requester.url
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
                when (position) {
                    0 -> {
                        presenter.requestPreviousChapter()
                    }
                    viewPager.adapter.count - 1 -> {
                        presenter.requestNextChapter()
                    }
                }
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

        presenter = NovelTextPresenter(this, novelListItem.requester, index)
        presenter.start()
    }

    fun showError(message: String, e: Throwable) {
        progressDialog.dismiss()
        alertError(alertDialog, message, e)
    }

    fun showPreviousChapter(chapter: NovelChapter, text: NovelText) {
        showNovelTexts(chapter, text)
        // 跳到最后一页，
        viewPager.currentItem = viewPager.adapter.count - 2
    }

    fun showNextChapter(chapter: NovelChapter, text: NovelText) {
        showNovelTexts(chapter, text)
        // 跳到第一页，0页不是漫画，
        viewPager.currentItem = 1
    }

    fun showNoPreviousChapter() {
        (viewPager.adapter as NovelTextPagerAdapter).noPrevious()
    }

    fun showNoNextChapter() {
        (viewPager.adapter as NovelTextPagerAdapter).noNext()
    }

    private fun showNovelTexts(chapter: NovelChapter, text: NovelText) {
        title = "$novelName - ${chapter.name}"
        urlTextView.text = chapter.requester.url
        progressDialog.dismiss()
        if (text.textList.isEmpty()) {
            alert(alertDialog, R.string.novel_not_support)
            // 无法浏览的情况显示状态栏标题栏导航栏，方便离开，
            show()
            return
        }
        viewPager.adapter = NovelTextPagerAdapter(this, text)
    }
}

class NovelTextPagerAdapter(private val ctx: Activity, private val novelText: NovelText) : PagerAdapter(), AnkoLogger {
    private val view: View by lazy {
        View.inflate(ctx, R.layout.novel_text_page_item, null).apply {
            textListView.setOnItemClickListener { _, _, _, _ ->
                (context as NovelTextActivity).toggle()
            }
        }
    }
    private val firstPage: View by lazy {
        View.inflate(ctx, R.layout.novel_text_item_loading, null).apply {
            loadingTextView.setText(R.string.now_loading_previous_issue)
        }
    }
    private val lastPage: View by lazy {
        View.inflate(ctx, R.layout.novel_text_item_loading, null).apply {
            loadingTextView.setText(R.string.now_loading_next_issue)
        }
    }

    override fun isViewFromObject(view: View, obj: Any) = view === obj
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        when (position) {
            0 -> {
                container.addView(firstPage)
                return firstPage
            }
            count - 1 -> {
                container.addView(lastPage)
                return lastPage
            }
        }
        val root = view
        root.textListView.adapter = NovelTextListAdapter(ctx, novelText.textList)
        container.addView(root)
        return root
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any?) {
        val view = obj as View
        container.removeView(view)
        when (position) {
            0 -> {
            }
            count - 1 -> {
            }
            else -> {
            }
        }
    }

    override fun getCount() = 1 + 2
    fun noNext() {
        lastPage.loadingTextView.setText(R.string.no_next_issue)
        lastPage.loadingProgressBar.hide()
    }

    fun noPrevious() {
        firstPage.loadingTextView.setText(R.string.no_previous_issue)
        firstPage.loadingProgressBar.hide()
    }

    fun setTextSize(size: Int) {
        debug { "NovelTextPagerAdapter.setTextSize $size" }
        (view.textListView.adapter as NovelTextListAdapter).setTextSize(size)
    }
}

class NovelTextListAdapter(private val ctx: Context, textList: List<String>) : BaseAdapter(), AnkoLogger {
    private val items = textList
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
}
