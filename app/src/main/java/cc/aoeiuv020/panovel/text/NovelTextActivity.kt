@file:Suppress("DEPRECATION")

package cc.aoeiuv020.panovel.text

import android.app.ProgressDialog
import android.os.Bundle
import android.os.Handler
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import cc.aoeiuv020.panovel.IView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.NovelDetail
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.local.*
import cc.aoeiuv020.panovel.util.*
import kotlinx.android.synthetic.main.activity_novel_text.*
import kotlinx.android.synthetic.main.novel_text_read_settings.*
import org.jetbrains.anko.browse
import org.jetbrains.anko.debug


/**
 *
 * Created by AoEiuV020 on 2017.10.03-19:06:44.
 */
class NovelTextActivity : NovelTextBaseFullScreenActivity(), IView {
    private lateinit var alertDialog: AlertDialog
    private lateinit var progressDialog: ProgressDialog
    private lateinit var presenter: NovelTextPresenter
    private lateinit var novelName: String
    private lateinit var chaptersAsc: List<NovelChapter>
    private lateinit var ntpAdapter: NovelTextPagerAdapter
    private var novelDetail: NovelDetail? = null
    private lateinit var novelItem: NovelItem
    private lateinit var progress: NovelProgress

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("progress", progress.toJson())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        alertDialog = AlertDialog.Builder(this).create()
        progressDialog = ProgressDialog(this)

        novelItem = intent.getStringExtra("novelItem").toBean()
        // 进度，读取顺序， savedInstanceState > intent > ReadProgress
        progress = savedInstanceState?.run { getString("progress").toBean<NovelProgress>() }
                ?: (intent.getSerializableExtra("index") as? Int)?.let { NovelProgress(it) } ?: Cache.progress.get(novelItem) ?: NovelProgress()
        debug { "receive $novelItem, $progress" }
        val requester = novelItem.requester
        novelName = novelItem.name

        urlTextView.text = requester.url
        urlBar.setOnClickListener {
            browse(urlTextView.text.toString())
        }

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
            alertColorPicker(Settings.backgroundColor) { color ->
                Settings.backgroundColor = color
                backgroundColorTextView.text = getString(R.string.background_color_placeholder, color)
                viewPager.setBackgroundColor(color)
            }
        }

        // 设置文字颜色，
        textColorTextView.text = getString(R.string.text_color_placeholder, Settings.textColor)
        textColorTextView.setOnClickListener {
            alertColorPicker(Settings.textColor) { color ->
                Settings.textColor = color
                textColorTextView.text = getString(R.string.text_color_placeholder, color)
                ntpAdapter.setTextColor(color)
            }
        }

        presenter = NovelTextPresenter(novelItem)
        ntpAdapter = NovelTextPagerAdapter(this, presenter)
        viewPager.adapter = ntpAdapter

        loading(progressDialog, R.string.novel_page)
        presenter.attach(this)
        presenter.start()
    }

    override fun onDestroy() {
        presenter.detach()
        // 清空viewPager，自动调用destroyItem切断presenter,
        viewPager.adapter = null
        super.onDestroy()
    }

    private fun currentChapterIndex(index: Int) {
        progress.chapter = index
        val chapter = chaptersAsc[index]
        title = "$novelName - ${chapter.name}"
        urlTextView.text = chapter.requester.url
    }

    fun showError(message: String, e: Throwable) {
        progressDialog.dismiss()
        alertError(alertDialog, message, e)
        show()
    }

    fun showDetail(detail: NovelDetail) {
        this.novelDetail = detail
        History.add(detail.novel)
        presenter.requestChapters(detail.requester)
    }

    fun showChaptersAsc(chaptersAsc: List<NovelChapter>) {
        this.chaptersAsc = chaptersAsc
        currentChapterIndex(progress.chapter)
        progressDialog.dismiss()
        if (chaptersAsc.isEmpty()) {
            alert(alertDialog, R.string.novel_not_support)
            // 无法浏览的情况显示状态栏标题栏导航栏，方便离开，
            show()
            return
        }
        ntpAdapter.setChaptersAsc(chaptersAsc)
        viewPager.currentItem = progress.chapter
        viewPager.post {
            ntpAdapter.setTextProgress(progress.text)
        }
    }

    override fun onPause() {
        super.onPause()
        // 比如断网，如果没有展示出章节，就直接保存持有的进度，
        ntpAdapter.getTextProgress()?.let { progress.text = it }
        debug {
            "save progress $progress"
        }
        Cache.progress.put(novelItem, progress)
    }

    private fun refresh() {
        loading(progressDialog, R.string.novel_page)
        // 保存一下的进度，
        ntpAdapter.getTextProgress()?.let { progress.text = it }
        presenter.refresh()
    }

    private fun download() {
        val index = viewPager.currentItem
        notify(1, getString(R.string.downloading_from_current_chapter_placeholder, index)
                , novelItem.name
                , R.drawable.ic_file_download)
        presenter.download(index)
    }

    private val handler: Handler = Handler()

    /**
     * 安卓通知是不按顺序的，使用唯一runnable确保顺序，
     */
    private val downloadingRunnable = object : Runnable {
        var exists = 0
        var downloads = 0
        var errors = 0
        var left = 0
        fun set(exists: Int, downloads: Int, errors: Int, left: Int) {
            this.exists = exists
            this.downloads = downloads
            this.errors = errors
            this.left = left
        }

        override fun run() {
            notify(1, getString(R.string.downloading_placeholder, exists, downloads, errors, left)
                    , novelItem.name
                    , R.drawable.ic_file_download)
        }
    }

    fun showDownloading(exists: Int, downloads: Int, errors: Int, left: Int) {
        handler.removeCallbacks(downloadingRunnable)
        downloadingRunnable.set(exists, downloads, errors, left)
        handler.postDelayed(downloadingRunnable, 100)
    }

    fun showDownloadComplete(exists: Int, downloads: Int, errors: Int) {
        handler.removeCallbacks(downloadingRunnable)
        notify(1, getString(R.string.download_complete_placeholder, exists, downloads, errors)
                , novelItem.name)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.refresh -> refresh()
            R.id.download -> download()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_text, menu)
        return true
    }
}

