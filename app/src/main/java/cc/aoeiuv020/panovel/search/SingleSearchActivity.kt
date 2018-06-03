package cc.aoeiuv020.panovel.search

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebViewClient
import cc.aoeiuv020.panovel.IView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.detail.NovelDetailActivity
import cc.aoeiuv020.panovel.report.Reporter
import kotlinx.android.synthetic.main.activity_single_search.*
import org.jetbrains.anko.*

/**
 * 负责单个网站的搜索功能，
 * 还有登录也在这里，
 */
class SingleSearchActivity : AppCompatActivity(), IView, AnkoLogger {
    companion object {
        fun start(ctx: Context, site: String) {
            ctx.startActivity<SingleSearchActivity>("site" to site)
        }
    }

    private lateinit var siteName: String
    private lateinit var presenter: SingleSearchPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_search)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        siteName = intent?.getStringExtra("site") ?: run {
            Reporter.unreachable()
            finish()
            return
        }
        debug { "receive site: $siteName" }
        title = siteName

        srlRefresh.isRefreshing = true
        srlRefresh.setOnRefreshListener {
            wvSite.reload()
            srlRefresh.isRefreshing = false
        }

        initWebView()

        presenter = SingleSearchPresenter(siteName)
        presenter.attach(this)

        presenter.start()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        wvSite.apply {
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
            settings.apply {
                javaScriptEnabled = true
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    @Suppress("DEPRECATION")
                    databasePath = ctx.cacheDir.resolve("webView").path
                }
                databaseEnabled = true
                domStorageEnabled = true
                cacheMode = WebSettings.LOAD_DEFAULT
                setAppCacheEnabled(true)
            }
        }
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setAcceptThirdPartyCookies(wvSite, true)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        presenter.pushCookies()
    }

    override fun onPause() {
        presenter.pullCookies()

        super.onPause()
    }

    fun showRemoveCookiesDone() {
        alert(
                title = getString(R.string.success),
                message = ctx.getString(R.string.message_cookies_removed)
        ) {
            okButton { }
        }.show()
    }

    fun getCurrentUrl(): String? = wvSite.url

    fun openPage(url: String) {
        srlRefresh.isRefreshing = false
        wvSite.loadUrl(url)
    }

    private fun open() {
        // TODO: 这里需要个loading dialog,
        getCurrentUrl()?.let { url ->
            presenter.open(url)
        }
    }

    private fun removeCookies() {
        presenter.removeCookies()
    }

    fun openNovelDetail(novel: Novel) {
        NovelDetailActivity.start(ctx, novel)
    }

    fun showError(message: String, e: Throwable) {
        alert(
                title = ctx.getString(R.string.error),
                message = message + e.message
        ) {
            okButton { }
        }.show()
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
        menuInflater.inflate(R.menu.menu_single_search, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search -> FuzzySearchActivity.startSingleSite(ctx, siteName)
            R.id.open -> open()
            R.id.close -> finish()
            R.id.removeCookies -> removeCookies()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
