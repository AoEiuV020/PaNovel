package cc.aoeiuv020.panovel.search

import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import cc.aoeiuv020.panovel.IView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelItem
import com.miguelcatalan.materialsearchview.MaterialSearchView
import kotlinx.android.synthetic.main.activity_refine_search.*
import kotlinx.android.synthetic.main.novel_item_list.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.startActivity

class RefineSearchActivity : AppCompatActivity(), IView, AnkoLogger {
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
    private lateinit var mAdapter: RefineSearchAdapter
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
        mAdapter = RefineSearchAdapter(this, presenter)
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
    }

    override fun onRestart() {
        super.onRestart()
        refresh()
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

    fun showError(message: String, e: Throwable) {
        snack.setText(message + e.message)
        snack.show()
        showOnComplete()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_refine_search, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search -> searchView.showSearch()
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}
