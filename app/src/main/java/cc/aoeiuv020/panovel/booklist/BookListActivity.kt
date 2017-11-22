package cc.aoeiuv020.panovel.booklist

import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.base.item.BaseItemListView
import cc.aoeiuv020.panovel.base.item.DefaultItemListAdapter
import cc.aoeiuv020.panovel.local.BookListData
import cc.aoeiuv020.panovel.local.toBean
import cc.aoeiuv020.panovel.local.toJson
import kotlinx.android.synthetic.main.novel_item_list.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.startActivity

/**
 *
 * Created by AoEiuV020 on 2017.11.22-14:49:22.
 */
class BookListActivity : AppCompatActivity(), BaseItemListView, AnkoLogger {
    companion object {
        fun start(context: Context, bookListData: BookListData) {
            context.startActivity<BookListActivity>("bookListData" to bookListData.toJson())
        }
    }

    private lateinit var presenter: BookListActivityPresenter
    private lateinit var mAdapter: DefaultItemListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.novel_item_list)

        val bookListData: BookListData = intent.getStringExtra("bookListData").toBean()

        recyclerView.setLayoutManager(LinearLayoutManager(this))
        presenter = BookListActivityPresenter(bookListData)
        mAdapter = DefaultItemListAdapter(this, presenter)
        recyclerView.setAdapter(mAdapter)
        recyclerView.setRefreshAction {
            forceRefresh()
        }

        presenter.attach(this)
    }

    override fun onDestroy() {
        presenter.detach()
        super.onDestroy()
    }

    override fun onStart() {
        super.onStart()
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

    private val snack: Snackbar by lazy {
        Snackbar.make(recyclerView, "", Snackbar.LENGTH_SHORT)
    }

    override fun showError(message: String, e: Throwable) {
        snack.setText(message + e.message)
        snack.show()
    }
}