package cc.aoeiuv020.panovel.booklist

import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import cc.aoeiuv020.panovel.App
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.base.item.BaseItemListView
import cc.aoeiuv020.panovel.base.item.DefaultItemListAdapter
import cc.aoeiuv020.panovel.base.item.OnItemLongClickListener
import cc.aoeiuv020.panovel.local.*
import cc.aoeiuv020.panovel.util.getStringExtra
import cc.aoeiuv020.panovel.util.show
import com.google.android.gms.ads.AdListener
import kotlinx.android.synthetic.main.activity_book_list.*
import kotlinx.android.synthetic.main.novel_item_list.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.selector
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

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

    private lateinit var bookListName: String
    private lateinit var presenter: BookListActivityPresenter
    private lateinit var mAdapter: DefaultItemListAdapter

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("bookListName", bookListName)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_list)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 没有这个参数就直接结束，
        bookListName = getStringExtra("bookListName", savedInstanceState) ?: run {
            // 不应该会到这里，
            toast("奇怪，重新打开试试，")
            finish()
            return
        }

        title = bookListName

        recyclerView.setLayoutManager(LinearLayoutManager(this))
        presenter = BookListActivityPresenter(bookListName)
        mAdapter = DefaultItemListAdapter(this, presenter, this)
        recyclerView.setAdapter(mAdapter)
        recyclerView.setRefreshAction {
            forceRefresh()
        }

        ad_view.adListener = object : AdListener() {
            override fun onAdLoaded() {
                ad_view.show()
            }
        }

        if (Settings.adEnabled) {
            ad_view.loadAd(App.adRequest)
        }

        presenter.attach(this)
        refresh()
    }

    override fun onPause() {
        ad_view.pause()
        if (Settings.bookListAutoSave) {
            save()
        }
        super.onPause()
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

    fun showNovelList(list: List<NovelHistory>) {
        mAdapter.data = ArrayList(list)
        recyclerView.dismissSwipeRefresh()
        recyclerView.showNoMore()
    }

    fun showSaveComplete(size: Int) {
        snack.setText("保存成功，共${size}本")
        snack.show()
    }

    private fun remove(position: Int) {
        presenter.remove(position)
        mAdapter.remove(position)
    }

    private fun save() {
        presenter.save()
    }

    private fun selectToAdd(list: List<NovelItem>) {
        AlertDialog.Builder(this)
                .setTitle(R.string.contents)
                .setMultiChoiceItems(Array(list.size) { list[it].bookId.toString() },
                        BooleanArray(list.size) { presenter.contains(list[it]) }, { _, i, isChecked ->
                    if (isChecked) {
                        presenter.add(list[i])
                    } else {
                        presenter.remove(list[i])
                    }
                }).setCancelable(false)
                .setPositiveButton(android.R.string.yes) { _, _ ->
                    presenter.addOk()
                }
                .create().apply {
                    listView.isFastScrollEnabled = true
                }.show()
    }


    private fun add() {
        val list = listOf(R.string.bookshelf to {
            selectToAdd(Bookshelf.list())
        }, R.string.history to {
            selectToAdd(History.list().map(NovelHistory::novel))
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
            android.R.id.home -> onBackPressed()
            R.id.add -> add()
            R.id.save -> save()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}