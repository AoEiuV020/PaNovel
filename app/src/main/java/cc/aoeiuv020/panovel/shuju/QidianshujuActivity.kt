package cc.aoeiuv020.panovel.shuju

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import cc.aoeiuv020.panovel.IView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.detail.NovelDetailActivity
import cc.aoeiuv020.panovel.settings.OtherSettings
import cc.aoeiuv020.panovel.util.safelyShow
import cc.aoeiuv020.regex.pick
import kotlinx.android.synthetic.main.activity_single_search.*
import org.jetbrains.anko.*

/**
 * 浏览起点数据，
 */
class QidianshujuActivity : AppCompatActivity(), IView, AnkoLogger {
    companion object {
        fun start(ctx: Context) {
            ctx.startActivity<QidianshujuActivity>()
        }
        fun start(ctx: Context, url: String) {
            ctx.startActivity<QidianshujuActivity>("url" to url)
        }
    }

    private var itemJumpQidian: MenuItem? = null
    private lateinit var presenter: QidianshujuPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qidianshuju)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setTitle(R.string.qidianshuju)

        srlRefresh.isRefreshing = true
        srlRefresh.setOnRefreshListener {
            wvSite.reload()
            srlRefresh.isRefreshing = false
        }

        initWebView()

        presenter = QidianshujuPresenter("起点中文")
        presenter.attach(this)

        presenter.start(intent.getStringExtra("url"))
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        wvSite.apply {
            webViewClient = MyWebViewClient()
            webChromeClient = MyWebChromeClient()
            settings.apply {
                javaScriptEnabled = true
            }
        }
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setAcceptThirdPartyCookies(wvSite, true)
            }
        }
    }

    class MyWebChromeClient : WebChromeClient() {
        override fun onJsAlert(
            view: WebView?,
            url: String?,
            message: String?,
            result: JsResult?
        ): Boolean {
            result?.cancel()
            return true
        }
    }

    private inner class MyWebViewClient : WebViewClient(), AnkoLogger {
        // 过时代替方法使用要api>=21,
        @Suppress("OverridingDeprecatedMember")
        override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
            debug { "shouldOverrideUrlLoading $url" }
            if (url == null) {
                return false
            }
            try {
                // http://www.qidianshuju.cn/book/1027440366.html
                // http://www.qidianshuju.com/book/1021708634.html
                val bookId = url.pick("http.*/book/(\\d*).html").first()
                openBook(bookId)
                return true
            } catch (ignored: Exception) {
            }
            return false
        }
    }

    private fun openBook(bookId: String) {
        // https://book.qidian.com/info/1027440366
        // https://m.qidian.com/book/1027440366
        if (OtherSettings.jumpQidian) {
            try {
                // I/ActivityTaskManager: START u0 {act=android.intent.action.VIEW cat=[android.intent.category.BROWSABLE] dat=QDReader://app/showBook?query={"bookId":1027440366} flg=0x14400000 cmp=com.qidian.QDReader/.ui.activity.MainGroupActivity (has extras)} from uid 10241
                // intent://app/showBook?query=%7B%22bookId%22%3A1027440366%7D#Intent;scheme=QDReader;S.browser_fallback_url=http%3A%2F%2Fdownload.qidian.com%2Fapknew%2Fsource%2FQDReaderAndroid.apk;end
                val intent = Intent.parseUri(
                    "intent://app/showBook?query=%7B%22bookId%22%3A$bookId%7D#Intent;scheme=QDReader;S.browser_fallback_url=http%3A%2F%2Fdownload.qidian.com%2Fapknew%2Fsource%2FQDReaderAndroid.apk;end",
                    Intent.URI_INTENT_SCHEME
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                return
            } catch (e: ActivityNotFoundException) {
                toast(R.string.qidian_not_found)
                OtherSettings.jumpQidian = false
                updateItem()
            }
        }
        presenter.open("https://book.qidian.com/info/$bookId")
    }

    fun openPage(url: String) {
        srlRefresh.isRefreshing = false
        wvSite.loadUrl(url)
    }

    fun openNovelDetail(novel: Novel) {
        NovelDetailActivity.start(ctx, novel)
    }

    fun showError(message: String, e: Throwable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && isDestroyed) {
            // 太丑了，
            return
        }
        alert(
            title = ctx.getString(R.string.error),
            message = message + e.message
        ) {
            okButton { }
        }.safelyShow()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        if (wvSite.canGoBack()) {
            wvSite.goBack()
        } else {
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_qidianshuju, menu)
        itemJumpQidian = menu.findItem(R.id.qidian)
        updateItem()
        return true
    }

    private fun updateItem() {
        itemJumpQidian?.setIcon(
            if (OtherSettings.jumpQidian) {
                R.drawable.ic_jump_qidian
            } else {
                R.drawable.ic_jump_qidian_blocked
            }
        )
    }

    private fun toggleQidian() {
        OtherSettings.jumpQidian = !OtherSettings.jumpQidian
        updateItem()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.close -> finish()
            R.id.browse -> presenter.browse()
            R.id.qidian -> toggleQidian()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
