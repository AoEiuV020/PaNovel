package cc.aoeiuv020.panovel.shuju

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import cc.aoeiuv020.panovel.IView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.detail.NovelDetailActivity
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
    }

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

        presenter.start()
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
                // https://book.qidian.com/info/1027440366
                presenter.open("https://book.qidian.com/info/$bookId")
                return true
            } catch (ignored: Exception) {
            }
            return false
        }
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
}
