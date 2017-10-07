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
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.SeekBar
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.NovelText
import cc.aoeiuv020.panovel.local.Bookshelf
import cc.aoeiuv020.panovel.local.NovelLocal
import cc.aoeiuv020.panovel.local.Settings
import cc.aoeiuv020.panovel.presenter.NovelTextPresenter
import cc.aoeiuv020.panovel.ui.base.NovelTextBaseFullScreenActivity
import cc.aoeiuv020.panovel.ui.widget.ColorPickerDialog
import kotlinx.android.synthetic.main.activity_novel_text.*
import kotlinx.android.synthetic.main.novel_text_header.view.*
import kotlinx.android.synthetic.main.novel_text_item.view.*
import kotlinx.android.synthetic.main.novel_text_page_item.view.*
import kotlinx.android.synthetic.main.novel_text_read_settings.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.browse
import org.jetbrains.anko.debug
import org.jetbrains.anko.dip
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
    private lateinit var ntpAdapter: NovelTextPagerAdapter
    private lateinit var novelLocal: NovelLocal
    private var index: Int by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        novelLocal = (intent.getSerializableExtra("novelLocal") as NovelLocal).let { Bookshelf.get(it) ?: it }
        debug { "receive $novelLocal" }
        val novelItem = novelLocal.novelDetail.novel
        val requester = novelLocal.novelDetail.novel.requester
        novelName = novelItem.name
        index = intent.getIntExtra("index", novelLocal.progress.chapterProgress)

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
                currentChapterIndex(position)
            }
        })

        // 设置字体大小，
        val textSize = Settings.textSize
        debug { "load textSite = $textSize" }
        textSizeTextView.text = getString(R.string.text_size_placeholders, textSize)
        textSizeSeekBar.progress = textSize - 12
        textSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val iTextSize = 12 + progress
                textSizeTextView.text = getString(R.string.text_size_placeholders, iTextSize)
                ntpAdapter.setTextSize(iTextSize)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val iTextSize = 12 + seekBar.progress
                Settings.textSize = iTextSize
            }
        })

        // 设置行间距，
        val lineSpacing = Settings.lineSpacing
        lineSpacingTextView.text = getString(R.string.line_spacing_placeholder, lineSpacing)
        lineSpacingSeekBar.progress = lineSpacing
        lineSpacingSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                lineSpacingTextView.text = getString(R.string.line_spacing_placeholder, progress)
                ntpAdapter.setLineSpacing(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                Settings.lineSpacing = seekBar.progress
            }
        })

        // 设置段间距，
        val paragraphSpacing = Settings.paragraphSpacing
        paragraphSpacingTextView.text = getString(R.string.paragraph_spacing_placeholder, paragraphSpacing)
        paragraphSpacingSeekBar.progress = paragraphSpacing
        paragraphSpacingSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                paragraphSpacingTextView.text = getString(R.string.paragraph_spacing_placeholder, progress)
                ntpAdapter.setParagraphSpacing(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                Settings.paragraphSpacing = seekBar.progress
            }
        })

        // 设置背景色，
        val backgroundColor = Settings.backgroundColor
        viewPager.setBackgroundColor(backgroundColor)
        backgroundColorTextView.text = getString(R.string.background_color_placeholder, backgroundColor)
        backgroundColorTextView.setOnClickListener {
            ColorPickerDialog(this@NovelTextActivity, Settings.backgroundColor, getString(R.string.background_color)) { color ->
                Settings.backgroundColor = color
                backgroundColorTextView.text = getString(R.string.background_color_placeholder, color)
                viewPager.setBackgroundColor(color)
            }.show()
        }

        // 设置文字颜色，
        textColorTextView.text = getString(R.string.text_color_placeholder, Settings.textColor)
        textColorTextView.setOnClickListener {
            ColorPickerDialog(this@NovelTextActivity, Settings.textColor, getString(R.string.text_color)) { color ->
                Settings.textColor = color
                textColorTextView.text = getString(R.string.text_color_placeholder, color)
                ntpAdapter.setTextColor(color)
            }.show()
        }

        presenter = NovelTextPresenter(this, requester, index)
        presenter.start()
    }

    private fun currentChapterIndex(index: Int) {
        val chapter = chaptersAsc[index]
        title = "$novelName - ${chapter.name}"
        urlTextView.text = chapter.requester.url
        novelLocal.progress.chapterProgress = index
        novelLocal.progress.textProgress = 0
    }

    fun showError(message: String, e: Throwable) {
        progressDialog.dismiss()
        alertError(alertDialog, message, e)
        show()
    }

    fun showChapters(chaptersAsc: List<NovelChapter>) {
        this.chaptersAsc = chaptersAsc
        currentChapterIndex(index)
        progressDialog.dismiss()
        if (chaptersAsc.isEmpty()) {
            alert(alertDialog, R.string.novel_not_support)
            // 无法浏览的情况显示状态栏标题栏导航栏，方便离开，
            show()
            return
        }
        viewPager.adapter = NovelTextPagerAdapter(this, presenter, chaptersAsc)
                .also { ntpAdapter = it }
        viewPager.currentItem = index
    }

    override fun onPause() {
        super.onPause()
        if (Bookshelf.contains(novelLocal)) {
            Bookshelf.add(novelLocal)
        }
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
                setOnClickListener {
                }
                textListView.setOnTouchListener(object : View.OnTouchListener {
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
            })
        }.also { usedHolders.push(it) }
        val chapter = chaptersAsc[position]
        debug {
            "instantiate $position $chapter"
        }
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
        (usedHolders + unusedHolders).forEach {
            it.setTextSize(size)
        }
    }

    fun setLineSpacing(size: Int) {
        (usedHolders + unusedHolders).forEach {
            it.setLineSpacing(size)
        }
    }

    fun setParagraphSpacing(size: Int) {
        (usedHolders + unusedHolders).forEach {
            it.setParagraphSpacing(size)
        }
    }

    fun setTextColor(color: Int) {
        (usedHolders + unusedHolders).forEach {
            it.setTextColor(color)
        }
    }

    class ViewHolder(private val ctx: NovelTextActivity, presenter: NovelTextPresenter, val view: View) : AnkoLogger {
        private val presenter = presenter.subPresenter(this)
        private val headerView by lazy {
            View.inflate(ctx, R.layout.novel_text_header, null)
        }
        private val textListAdapter = NovelTextListAdapter(ctx)
        private var paragraphSpacing = Settings.paragraphSpacing

        init {
            view.textListView.apply {
                addHeaderView(headerView)
                dividerHeight = ctx.dip(paragraphSpacing)
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

        fun setLineSpacing(size: Int) {
            textListAdapter.setLineSpacing(size)
        }

        fun setParagraphSpacing(size: Int) {
            view.textListView.dividerHeight = ctx.dip(size)
        }

        fun setTextColor(color: Int) {
            textListAdapter.setTextColor(color)
        }
    }
}

class NovelTextListAdapter(private val ctx: Context) : BaseAdapter(), AnkoLogger {
    private var items = emptyList<String>()
    private var textSize = Settings.textSize
    private var lineSpacing = Settings.lineSpacing
    private var textColor = Settings.textColor

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View
            = (convertView ?: LayoutInflater.from(ctx).inflate(R.layout.novel_text_item, parent, false)).apply {
        textView.text = "        " + getItem(position)
        textView.textSize = textSize.toFloat()
        textView.setTextColor(textColor)
        textView.setLineSpacing(ctx.dip(lineSpacing).toFloat(), 1.toFloat())
    }

    override fun getItem(position: Int) = items[position]

    override fun getItemId(position: Int) = 0L

    override fun getCount() = items.size

    fun setTextSize(size: Int) {
        debug { "NovelTextListAdapter.setTextSize $size" }
        this.textSize = size
        notifyDataSetChanged()
    }

    fun setLineSpacing(size: Int) {
        this.lineSpacing = size
        notifyDataSetChanged()
    }

    fun setTextColor(color: Int) {
        this.textColor = color
        notifyDataSetChanged()
    }

    fun setNovelText(novelText: NovelText) {
        debug { items.size }
        items = novelText.textList.let { if (it is RandomAccess) it else ArrayList(it) }
        notifyDataSetChanged()
    }

    override fun isEnabled(position: Int): Boolean = false
}
