package cc.aoeiuv020.panovel.search

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.webkit.CookieManager
import android.webkit.WebViewClient
import cc.aoeiuv020.panovel.IView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.api.NovelSite
import cc.aoeiuv020.panovel.detail.NovelDetailActivity
import cc.aoeiuv020.panovel.local.toBean
import cc.aoeiuv020.panovel.local.toJson
import cc.aoeiuv020.panovel.util.getStringExtra
import com.miguelcatalan.materialsearchview.MaterialSearchView
import kotlinx.android.synthetic.main.activity_single_search.*
import org.jetbrains.anko.*

/**
 * 负责单个网站的搜索功能，
 * 还有登录也在这里，
 */
class SingleSearchActivity : AppCompatActivity(), IView, AnkoLogger {
    companion object {
        fun start(ctx: Context, site: NovelSite) {
            ctx.startActivity<SingleSearchActivity>("site" to site.toJson())
        }
    }

    private lateinit var site: NovelSite
    private lateinit var presenter: SingleSearchPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_search)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        site = getStringExtra("site", savedInstanceState)?.toBean()
                ?: throw IllegalArgumentException("必须传入一个网站，")

        title = site.name

        // TODO: 这里没启用，现在是点击搜索图标直接跳到模糊搜索，
        searchView.setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchView.hideKeyboard(searchView)
                FuzzySearchActivity.start(ctx, site, query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = false
        })

        srlRefresh.isRefreshing = true
        srlRefresh.setOnRefreshListener {
            wvSite.reload()
            srlRefresh.isRefreshing = false
        }

        initWebView()

        presenter = SingleSearchPresenter(site)
        presenter.attach(this)

        presenter.start()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        wvSite.webViewClient = WebViewClient()
        wvSite.settings.javaScriptEnabled = true
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
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

    fun openNovelDetail(novelItem: NovelItem) {
        NovelDetailActivity.start(ctx, novelItem)
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
            R.id.search -> FuzzySearchActivity.start(ctx, site)
            R.id.open -> open()
            R.id.close -> finish()
            R.id.removeCookies -> removeCookies()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
