package cc.aoeiuv020.panovel.booklist

import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.base.item.BaseItemListView
import cc.aoeiuv020.panovel.base.item.DefaultItemListAdapter
import cc.aoeiuv020.panovel.base.item.OnItemLongClickListener
import cc.aoeiuv020.panovel.local.Bookshelf
import cc.aoeiuv020.panovel.local.History
import cc.aoeiuv020.panovel.local.bookId
import kotlinx.android.synthetic.main.novel_item_list.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.selector
import org.jetbrains.anko.startActivity

/**
 *
 * Created by AoEiuV020 on 2017.11.22-14:49:22.
 */
class BookListActivity : AppCompatActivity(), BaseItemListView, AnkoLogger, OnItemLongClickListener {
    companion object {
        fun start(context: Context, bookListName: String) {
            context.startActivity<BookListActivity>("bookListName" to bookListName)
        }
    }

    private lateinit var presenter: BookListActivityPresenter
    private lateinit var mAdapter: DefaultItemListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.novel_item_list)

        val bookListName = intent.getStringExtra("bookListName")

        title = bookListName

        recyclerView.setLayoutManager(LinearLayoutManager(this))
        presenter = BookListActivityPresenter(bookListName)
        mAdapter = DefaultItemListAdapter(this, presenter, this)
        recyclerView.setAdapter(mAdapter)
        recyclerView.setRefreshAction {
            forceRefresh()
        }

        presenter.attach(this)
        refresh()
    }

    override fun onDestroy() {
        presenter.detach()
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        save()
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
        mAdapter.data = ArrayList(list)
        recyclerView.dismissSwipeRefresh()
    }

    fun showSaveComplete(size: Int) {
        snack.setText("保存成功，共${size}本")
        snack.show()
    }


    private fun add(novelItem: NovelItem) {
        presenter.add(novelItem)
        mAdapter.add(novelItem)
    }

    private fun remove(position: Int) {
        presenter.remove(position)
        mAdapter.remove(position)
    }

    private fun save() {
        presenter.save()
    }

    private fun add() {
        val list = listOf(R.string.bookshelf to {
            val list = Bookshelf.list()
            selector(getString(R.string.bookshelf), list.map { it.bookId.toString() }) { _, i ->
                val novelItem = list[i]
                add(novelItem)
            }
        }, R.string.history to {
            History.list().let { list ->
                selector(getString(R.string.history), list.map { it.novel }.map { it.bookId.toString() }) { _, i ->
                    val novelItem = list[i].novel
                    add(novelItem)
                }
            }
        })
        selector(getString(R.string.add_from), list.unzip().first.map { getString(it) }) { _, i ->
            list[i].second.invoke()
        }
    }

    override fun onItemLongClick(position: Int, novelItem: NovelItem) {
        val list = listOf(R.string.remove to {
            remove(position)
        })
        selector(getString(R.string.action), list.unzip().first.map { getString(it) }) { _, i ->
            list[i].second.invoke()
        }
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
        menuInflater.inflate(R.menu.menu_book_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add -> add()
            R.id.save -> save()
        }
        return super.onOptionsItemSelected(item)
    }
}