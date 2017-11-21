@file:Suppress("DEPRECATION")

package cc.aoeiuv020.panovel.text

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuItem
import cc.aoeiuv020.panovel.IView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.NovelDetail
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.detail.NovelDetailActivity
import cc.aoeiuv020.panovel.local.*
import cc.aoeiuv020.panovel.util.alert
import cc.aoeiuv020.panovel.util.alertError
import cc.aoeiuv020.panovel.util.loading
import cc.aoeiuv020.panovel.util.notify
import kotlinx.android.synthetic.main.activity_novel_text.*
import org.jetbrains.anko.browse
import org.jetbrains.anko.debug
import org.jetbrains.anko.startActivity


/**
 *
 * Created by AoEiuV020 on 2017.10.03-19:06:44.
 */
class NovelTextActivity : NovelTextBaseFullScreenActivity(), IView {
    companion object {
        fun start(context: Context, novelItem: NovelItem) {
            context.startActivity<NovelTextActivity>("novelItem" to novelItem.toJson())
        }

        fun start(context: Context, novelItem: NovelItem, index: Int) {
            context.startActivity<NovelTextActivity>("novelItem" to novelItem.toJson(), "index" to index)
        }
    }

    private lateinit var alertDialog: AlertDialog
    private lateinit var progressDialog: ProgressDialog
    private lateinit var presenter: NovelTextPresenter
    private lateinit var novelName: String
    private lateinit var chaptersAsc: List<NovelChapter>
    private lateinit var ntpAdapter: NovelTextPagerAdapter
    private var novelDetail: NovelDetail? = null
    private lateinit var novelItem: NovelItem
    private lateinit var progress: NovelProgress
    private lateinit var navigation: NovelTextNavigation

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
                ?: (intent.getSerializableExtra("index") as? Int)?.let { NovelProgress(it) } ?: Progress.load(novelItem)
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
                onChapterSelected(position)
            }
        })

        navigation = NovelTextNavigation(this, novelItem, nav_view)

        presenter = NovelTextPresenter(novelItem)
        ntpAdapter = NovelTextPagerAdapter(this, presenter)
        viewPager.adapter = ntpAdapter

        loading(progressDialog, R.string.novel_chapters)
        presenter.attach(this)
        presenter.start()
    }

    override fun show() {
        super.show()
        navigation.reset()
    }

    fun setTextColor(color: Int) {
        ntpAdapter.setTextColor(color)
    }

    fun setBackgroundColor(color: Int) {
        viewPager.setBackgroundColor(color)
    }

    fun setParagraphSpacing(progress: Int) {
        ntpAdapter.setParagraphSpacing(progress)
    }

    fun setLineSpacing(progress: Int) {
        ntpAdapter.setLineSpacing(progress)
    }

    fun setTextSize(textSize: Int) {
        ntpAdapter.setTextSize(textSize)
    }

    override fun onDestroy() {
        presenter.detach()
        // 清空viewPager，自动调用destroyItem切断presenter,
        viewPager.adapter = null
        super.onDestroy()
    }

    private fun selectChapter(index: Int) {
        viewPager.setCurrentItem(index, false)
    }

    private fun onChapterSelected(index: Int) {
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
        // 支持跳到倒数第一章，
        if (progress.chapter == -1) {
            progress.chapter = chaptersAsc.lastIndex
        }
        onChapterSelected(progress.chapter)
        progressDialog.dismiss()
        if (chaptersAsc.isEmpty()) {
            alert(alertDialog, R.string.novel_not_support)
            // 无法浏览的情况显示状态栏标题栏导航栏，方便离开，
            show()
            return
        }
        ntpAdapter.setChaptersAsc(chaptersAsc)
        viewPager.setCurrentItem(progress.chapter, false)
        ntpAdapter.setTextProgress(progress.text)
    }

    override fun onPause() {
        super.onPause()
        // 比如断网，如果没有展示出章节，就直接保存持有的进度，
        ntpAdapter.getTextProgress()?.let { progress.text = it }
        debug {
            "save progress $progress"
        }
        Progress.save(novelItem, progress)
    }

    private fun refresh() {
        loading(progressDialog, R.string.novel_chapters)
        // 保存一下的进度，
        ntpAdapter.getTextProgress()?.let { progress.text = it }
        presenter.refresh()
    }

    fun detail() {
        NovelDetailActivity.start(this, novelItem)
    }

    fun download() {
        val index = viewPager.currentItem
        notify(1, getString(R.string.downloading_from_current_chapter_placeholder, index)
                , novelItem.name
                , R.drawable.ic_file_download)
        presenter.download(index)
    }

    fun showContents() {
        AlertDialog.Builder(this)
                .setTitle(R.string.contents)
                .setAdapter(NovelContentsAdapter(this, novelItem, chaptersAsc, progress.chapter)) { _, index ->
                    selectChapter(index)
                }.create().apply {
            listView.isFastScrollEnabled = true
            listView.post {
                listView.setSelection(progress.chapter)
            }
        }.show()
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
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_text, menu)
        return true
    }
}

