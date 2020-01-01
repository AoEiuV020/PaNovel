package cc.aoeiuv020.panovel.search

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import cc.aoeiuv020.panovel.IView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.data.NovelManager
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.list.NovelListAdapter
import cc.aoeiuv020.panovel.settings.ListSettings
import cc.aoeiuv020.panovel.settings.OtherSettings
import cc.aoeiuv020.panovel.util.getStringExtra
import com.google.android.material.snackbar.Snackbar
import com.miguelcatalan.materialsearchview.MaterialSearchView
import kotlinx.android.synthetic.main.activity_fuzzy_search.*
import kotlinx.android.synthetic.main.novel_item_list.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.ctx
import org.jetbrains.anko.startActivity


class FuzzySearchActivity : AppCompatActivity(), IView, AnkoLogger {
    companion object {
        fun start(ctx: Context) {
            ctx.startActivity<FuzzySearchActivity>()
        }

        fun start(ctx: Context, novel: Novel) {
            // 精确搜索，refine search,
            start(ctx, novel.name, novel.author)
        }

        fun start(ctx: Context, name: String) {
            // 模糊搜索，fuzzy search,
            ctx.startActivity<FuzzySearchActivity>("name" to name)
        }

        fun start(ctx: Context, name: String, author: String) {
            // 精确搜索，refine search,
            ctx.startActivity<FuzzySearchActivity>("name" to name, "author" to author)
        }

        fun startSingleSite(ctx: Context, site: String) {
            // 单个网站模糊搜索，fuzzy search,
            ctx.startActivity<FuzzySearchActivity>("site" to site)
        }
    }

    private lateinit var presenter: FuzzySearchPresenter
    private val novelListAdapter by lazy {
        NovelListAdapter(onError = ::showError)
    }

    private var name: String? = null
    private var author: String? = null
    private var site: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fuzzy_search)
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

        rvNovel.layoutManager = if (ListSettings.gridView) {
            androidx.recyclerview.widget.GridLayoutManager(ctx, if (ListSettings.largeView) 3 else 5)
        } else {
            androidx.recyclerview.widget.LinearLayoutManager(ctx)
        }
        presenter = FuzzySearchPresenter()
        presenter.attach(this)
        rvNovel.adapter = novelListAdapter

        name = getStringExtra("name", savedInstanceState)
        author = getStringExtra("author", savedInstanceState)
        site = getStringExtra("site", savedInstanceState)

        site?.let {
            presenter.singleSite(it)
        }
        srlRefresh.setOnRefreshListener {
            // 任何时候刷新都没影响，所以一开始就初始化好，
            forceRefresh()
        }

        // 如果传入了名字，就直接开始搜索，
        name?.let { nameNonnull ->
            search(nameNonnull, author)
        } ?: searchView.post { showSearch() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("name", name)
        outState.putString("author", author)
    }

    private fun showSearch() {
        searchView.showSearch()
        searchView.setQuery(presenter.name, false)
    }

    override fun onDestroy() {
        presenter.detach()
        super.onDestroy()
    }

    private fun search(name: String, author: String? = null) {
        srlRefresh.isRefreshing = true
        title = name
        this.name = name
        this.author = author
        novelListAdapter.clear()
        if (OtherSettings.refreshOnSearch) {
            novelListAdapter.refresh()
        }
        presenter.search(name, author)
    }

    private fun refresh() {
        // 重新搜索就是刷新了，
        // 没搜索就刷新也是不禁止的，所以要判断下，
        name?.let {
            search(it, author)
        } ?: run {
            srlRefresh.isRefreshing = false
        }
    }

    /**
     * 刷新列表，同时刷新小说章节信息，
     * 为了方便从书架过来，找一本小说的所有源的最新章节，
     */
    private fun forceRefresh() {
        novelListAdapter.refresh()
        refresh()
    }

    fun addResult(list: List<NovelManager>) {
        // 插入有时会导致下滑，原因不明，保存状态解决，
        val lm = rvNovel.layoutManager ?: return
        val state = lm.onSaveInstanceState() ?: return
        novelListAdapter.addAll(list)
        lm.onRestoreInstanceState(state)
    }

    fun showOnComplete() {
        srlRefresh.isRefreshing = false
    }

    private val snack: Snackbar by lazy {
        Snackbar.make(rvNovel, "", Snackbar.LENGTH_SHORT)
    }

    fun showError(message: String, e: Throwable) {
        srlRefresh.isRefreshing = false
        snack.setText(message + e.message)
        snack.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_fuzzy_search, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search -> searchView.showSearch()
            android.R.id.home -> onBackPressed()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
