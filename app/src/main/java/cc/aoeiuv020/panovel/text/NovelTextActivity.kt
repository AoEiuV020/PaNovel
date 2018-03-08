@file:Suppress("DEPRECATION")

package cc.aoeiuv020.panovel.text

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import cc.aoeiuv020.panovel.IView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.NovelDetail
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.detail.NovelDetailActivity
import cc.aoeiuv020.panovel.local.*
import cc.aoeiuv020.panovel.search.RefineSearchActivity
import cc.aoeiuv020.panovel.util.alert
import cc.aoeiuv020.panovel.util.alertError
import cc.aoeiuv020.panovel.util.loading
import cc.aoeiuv020.panovel.util.notify
import cc.aoeiuv020.reader.*
import kotlinx.android.synthetic.main.activity_novel_text.*
import org.jetbrains.anko.browse
import org.jetbrains.anko.debug
import org.jetbrains.anko.error
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
    private var novelDetail: NovelDetail? = null
    private lateinit var novelItem: NovelItem
    private lateinit var progress: NovelProgress
    private lateinit var navigation: NovelTextNavigation
    private lateinit var reader: INovelReader

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

        presenter = NovelTextPresenter(novelItem)

        initReader()

        navigation = NovelTextNavigation(this, novelItem, nav_view)

        loading(progressDialog, R.string.novel_chapters)
        presenter.attach(this)
        presenter.start()
    }

    private fun initReader() {
        reader = Readers.getReader(this, Novel(novelItem.name, novelItem.author), flContent, presenter.getRequester(), Settings.makeReaderConfig()).apply {
            menuListener = object : MenuListener {
                override fun hide() {
                    this@NovelTextActivity.hide()
                }

                override fun show() {
                    this@NovelTextActivity.show()
                }

                override fun toggle() {
                    this@NovelTextActivity.toggle()
                }
            }
            chapterChangeListener = object : ChapterChangeListener {
                override fun onChapterChange() {
                    onChapterSelected(reader.currentChapter)
                }
            }
        }
    }

    override fun onBackPressed() {
        if (Settings.backPressOutOfFullScreen && !mVisible) {
            show()
        } else {
            super.onBackPressed()
        }
    }

    override fun show() {
        super.show()
        navigation.reset(reader.maxTextProgress, reader.textProgress)
    }

    fun previousChapter() {
        selectChapter(reader.currentChapter - 1)
    }

    fun nextChapter() {
        selectChapter(reader.currentChapter + 1)
    }

    fun setTextProgress(progress: Int) {
        reader.textProgress = progress
    }

    fun refreshCurrentChapter() {
        reader.refreshCurrentChapter()
    }

    private fun resetReader() {
        reader.onDestroy()
        flContent.removeAllViews() // 多余，上面已经移除，
        initReader()
        showChaptersAsc(chaptersAsc)
    }

    fun setAnimationSpeed(animationSpeed: Float) {
        reader.config.animationSpeed = animationSpeed
    }

    fun setAnimationMode(animationMode: AnimationMode, oldAnimationMode: AnimationMode) {
        debug { "setAnimationMode $oldAnimationMode to $animationMode" }
        if ((animationMode == AnimationMode.SIMPLE && oldAnimationMode != AnimationMode.SIMPLE)
                || (animationMode != AnimationMode.SIMPLE && oldAnimationMode == AnimationMode.SIMPLE)) {
            resetReader()
        } else {
            reader.config.animationMode = animationMode
        }
    }

    fun setMargins(left: Int? = null, top: Int? = null, right: Int? = null, bottom: Int? = null) {
        left?.let { reader.config.leftSpacing = it }
        top?.let { reader.config.topSpacing = it }
        right?.let { reader.config.rightSpacing = it }
        bottom?.let { reader.config.bottomSpacing = it }
    }

    fun setTextColor(color: Int) {
        reader.config.textColor = color
    }

    fun setBackgroundColor(color: Int, fromUser: Boolean = false) {
        if (fromUser) {
            reader.config.backgroundImage = null
        }
        reader.config.backgroundColor = color
    }

    fun requestBackgroundImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, 0)
    }

    fun requestFont() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(intent, 1)
    }

    fun resetFont() {
        Settings.font = null
        setFont(null)
    }

    private var cacheUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            0 -> data?.data?.let { uri ->
                try {
                    Settings.backgroundImage = uri
                    setBackgroundImage(uri)
                } catch (e: SecurityException) {
                    error("读取背景图失败", e)
                    cacheUri = uri
                    ActivityCompat.requestPermissions(this, arrayOf(READ_EXTERNAL_STORAGE), requestCode)
                }
            }
            1 -> data?.data?.let { uri ->
                try {
                    Settings.font = uri
                    setFont(Settings.tfFont)
                } catch (e: SecurityException) {
                    error("读取字体失败", e)
                    cacheUri = uri
                    ActivityCompat.requestPermissions(this, arrayOf(READ_EXTERNAL_STORAGE), requestCode)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            0 -> cacheUri?.let { uri ->
                try {
                    Settings.backgroundImage = uri
                    setBackgroundImage(uri)
                } catch (e: SecurityException) {
                    error("读取背景图还是失败", e)
                    cacheUri = null
                }
            }
            1 -> cacheUri?.let { uri ->
                try {
                    Settings.font = uri
                    setFont(Settings.tfFont)
                } catch (e: SecurityException) {
                    error("读取字体还是失败", e)
                    cacheUri = null
                }
            }
        }
    }

    private fun setBackgroundImage(uri: Uri?) {
        reader.config.backgroundImage = uri
    }

    private fun setFont(font: Typeface?) {
        reader.config.font = font
    }

    fun setParagraphSpacing(progress: Int) {
        reader.config.paragraphSpacing = progress
    }

    fun setLineSpacing(progress: Int) {
        reader.config.lineSpacing = progress
    }

    fun setTextSize(textSize: Int) {
        reader.config.textSize = textSize
    }

    override fun onDestroy() {
        presenter.detach()
        reader.onDestroy()
        super.onDestroy()
    }

    /**
     * 这个是代码主动选择章节，
     */
    private fun selectChapter(index: Int) {
        if (index !in chaptersAsc.indices) {
            // 超出范围直接无视，
            return
        }
        reader.currentChapter = index
        onChapterSelected(index)
    }

    /**
     * 这个无论是用户翻页切换章节还是其他跳章节都要调用，
     */
    private fun onChapterSelected(index: Int) {
        debug { "onChapterSelected $index" }
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
        debug { "chapters loaded ${chaptersAsc.size}" }
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
        reader.chapterList = chaptersAsc.map { Chapter(it.name) }
        reader.currentChapter = progress.chapter
        reader.textProgress = progress.text
    }

    override fun onPause() {
        super.onPause()
        // 比如断网，如果没有展示出章节，就直接保存持有的进度，
        reader.textProgress.let { progress.text = it }
        debug {
            "save progress $progress"
        }
        Progress.save(novelItem, progress)
    }

    private fun refineSearch() {
        RefineSearchActivity.start(this, novelItem)
    }

    fun refreshChapterList() {
        loading(progressDialog, R.string.novel_chapters)
        // 保存一下的进度，
        reader.textProgress.let { progress.text = it }
        presenter.refreshChapterList()
    }

    fun detail() {
        NovelDetailActivity.start(this, novelItem)
    }

    fun download() {
        val index = reader.currentChapter
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (Settings.volumeKeyScroll) {
            when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_DOWN -> scrollNext()
                KeyEvent.KEYCODE_VOLUME_UP -> scrollPrev()
                else -> return super.onKeyDown(keyCode, event)
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (Settings.volumeKeyScroll) {
            return when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_DOWN -> true
                KeyEvent.KEYCODE_VOLUME_UP -> true
                else -> super.onKeyUp(keyCode, event)
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    private fun scrollNext() {
        reader.scrollNext()
    }

    private fun scrollPrev() {
        reader.scrollPrev()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.refresh -> refreshChapterList()
            R.id.search -> refineSearch()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_text, menu)
        return true
    }
}

