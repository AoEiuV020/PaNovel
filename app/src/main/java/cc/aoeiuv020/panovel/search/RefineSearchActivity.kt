package cc.aoeiuv020.panovel.search

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import cc.aoeiuv020.panovel.App
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.base.item.BaseItemListView
import cc.aoeiuv020.panovel.base.item.DefaultItemListAdapter
import cc.aoeiuv020.panovel.local.Settings
import cc.aoeiuv020.panovel.util.show
import com.google.android.gms.ads.AdListener
import com.miguelcatalan.materialsearchview.MaterialSearchView
import kotlinx.android.synthetic.main.activity_refine_search.*
import kotlinx.android.synthetic.main.novel_item_list.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast


class RefineSearchActivity : AppCompatActivity(), BaseItemListView, AnkoLogger {
    companion object {
        fun start(context: Context) {
            context.startActivity<RefineSearchActivity>()
        }

        fun start(context: Context, novelItem: NovelItem) {
            start(context, novelItem.name, novelItem.author)
        }

        fun start(context: Context, name: String) {
            context.startActivity<RefineSearchActivity>("name" to name)
        }

        fun start(context: Context, name: String, author: String) {
            context.startActivity<RefineSearchActivity>("name" to name, "author" to author)
        }
    }

    private lateinit var presenter: RefineSearchPresenter
    private lateinit var mAdapter: DefaultItemListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_refine_search)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        searchView.setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchView.hideKeyboard(searchView)
                search(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = false
        })

        recyclerView.setLayoutManager(LinearLayoutManager(this))
        presenter = RefineSearchPresenter()
        presenter.attach(this)
        mAdapter = DefaultItemListAdapter(this, presenter)
        recyclerView.setAdapter(mAdapter)
        recyclerView.setRefreshAction {
            forceRefresh()
        }

        val name = intent.getStringExtra("name")
        val author = intent.getStringExtra("author")
        name?.let { nameNonnull ->
            author?.let { authorNonnull ->
                search(nameNonnull, authorNonnull)
            } ?: search(nameNonnull)
        } ?: searchView.post { searchView.showSearch() }

        ad_view.adListener = object : AdListener() {
            override fun onAdLoaded() {
                ad_view.show()
            }
        }

        if (Settings.adEnabled) {
            ad_view.loadAd(App.adRequest)
        }
    }

    override fun onPause() {
        ad_view.pause()
        super.onPause()
    }

    override fun onRestart() {
        super.onRestart()
        refresh()
    }

    override fun onResume() {
        super.onResume()
        ad_view.resume()
    }

    override fun onDestroy() {
        presenter.detach()
        ad_view.destroy()
        super.onDestroy()
    }

    private fun search(name: String, author: String) {
        title = name
        mAdapter.clear()
        mAdapter.openLoadMore()
        presenter.search(name, author)
    }

    private fun search(name: String) {
        title = name
        mAdapter.clear()
        mAdapter.openLoadMore()
        presenter.search(name)
    }

    private fun refresh() {
        mAdapter.notifyDataSetChanged()
    }

    private fun scan() {
        val intent = Intent("com.google.zxing.client.android.SCAN")
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE")
        try {
            startActivityForResult(intent, 0)
        } catch (_: ActivityNotFoundException) {
            toast("没安装zxing二维码扫描器，")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        data?.extras?.getString("SCAN_RESULT")?.let {
            search(it)
        }
    }

    /**
     * 刷新列表，同时刷新小说章节信息，
     */
    private fun forceRefresh() {
        mAdapter.clear()
        mAdapter.openLoadMore()
        presenter.forceRefresh()
        recyclerView.dismissSwipeRefresh()
    }

    fun addNovel(item: NovelItem) {
        mAdapter.add(item)
    }

    fun showOnComplete() {
        recyclerView.dismissSwipeRefresh()
        recyclerView.showNoMore()
    }

    private val snack: Snackbar by lazy {
        Snackbar.make(recyclerView, "", Snackbar.LENGTH_SHORT)
    }

    override fun showError(message: String, e: Throwable) {
        snack.setText(message + e.message)
        snack.show()
        recyclerView.dismissSwipeRefresh()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_refine_search, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search -> searchView.showSearch()
            R.id.scan -> scan()
            android.R.id.home -> onBackPressed()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
