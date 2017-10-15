package cc.aoeiuv020.panovel.bookshelf

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import cc.aoeiuv020.panovel.IView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.bookstore.BookstoreActivity
import kotlinx.android.synthetic.main.activity_bookshelf.*
import kotlinx.android.synthetic.main.content_bookshelf.*
import org.jetbrains.anko.startActivity

/**
 *
 * Created by AoEiuV020 on 2017.10.14-21:54.
 */
class BookshelfActivity : AppCompatActivity(), IView {
    private lateinit var mAdapter: BookshelfAdapter
    private lateinit var presenter: BookshelfPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookshelf)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { _ ->
            startActivity<BookstoreActivity>()
        }

        recyclerView.setLayoutManager(LinearLayoutManager(this))
        presenter = BookshelfPresenter()
        mAdapter = BookshelfAdapter(this, presenter)
        recyclerView.setAdapter(mAdapter)
        recyclerView.setRefreshAction {
            forceRefresh()
        }

        recyclerView.showSwipeRefresh()
        presenter.attach(this)
        presenter.start()
    }

    override fun onDestroy() {
        presenter.detach()
        super.onDestroy()
    }

    override fun onRestart() {
        super.onRestart()
        refresh()
    }

    private fun refresh() {
        recyclerView.showSwipeRefresh()
        presenter.refresh()
    }

    /**
     * 强行刷新，重新下载小说详情，主要是看最新章，
     */
    private fun forceRefresh() {
        presenter.forceRefresh()
    }

    fun showNovelList(list: List<NovelItem>) {
        mAdapter.data = list
        recyclerView.dismissSwipeRefresh()
        recyclerView.showNoMore()
    }

    fun showError(message: String, e: Throwable) {
        Snackbar.make(fab, message + e.message, Snackbar.LENGTH_SHORT).show()
    }

}
