package cc.aoeiuv020.panovel.booklist

import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import cc.aoeiuv020.base.jar.toBean
import cc.aoeiuv020.base.jar.toJson
import cc.aoeiuv020.panovel.App
import cc.aoeiuv020.panovel.IView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.data.entity.BookList
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.list.DefaultNovelItemActionListener
import cc.aoeiuv020.panovel.list.NovelMutableListAdapter
import cc.aoeiuv020.panovel.list.NovelViewHolder
import cc.aoeiuv020.panovel.local.Settings
import cc.aoeiuv020.panovel.report.Reporter
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
class BookListActivity : AppCompatActivity(), IView, AnkoLogger {
    companion object {
        fun start(context: Context, bookListId: Long) {
            context.startActivity<BookListActivity>("bookListId" to bookListId)
        }
    }

    // onCreate里赋值，必须有值，
    private var bookListId: Long = -1
    private lateinit var presenter: BookListActivityPresenter

    // 不指定对象类型的话，下面的adapter传参会报类型检查出错，
    private val itemListener: DefaultNovelItemActionListener = object : DefaultNovelItemActionListener({ message, e ->
        showError(message, e)
    }) {
        override fun onItemLongClick(vh: NovelViewHolder): Boolean {
            // 长按弹出删除菜单，只要这个就够了，
            // TODO: 改成支持滑动删除感觉更好，
            val list = listOf(R.string.remove to {
                presenter.remove(vh.novel)
                mAdapter.remove(vh.layoutPosition)
            })
            selector(getString(R.string.action), list.unzip().first.map { getString(it) }) { _, i ->
                list[i].second.invoke()
            }
            return true
        }
    }
    private val mAdapter = NovelMutableListAdapter(R.layout.book_list_item, itemListener)

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // 貌似没必要，
        outState.putString("bookListId", bookListId.toJson(App.gson))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_list)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        bookListId = getStringExtra("bookListId", savedInstanceState)
                ?.toBean(App.gson)
                ?: run {
            Reporter.unreachable()
            toast("不存在，")
            finish()
            return
        }

        rvNovel.layoutManager = LinearLayoutManager(this)
        presenter = BookListActivityPresenter(bookListId)
        rvNovel.adapter = mAdapter
        srlRefresh.setOnRefreshListener {
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
        // 查询书单名，只用来改title, 没什么大用，
        presenter.start()
        // 要查询书单中的小说列表，
        refresh()
    }

    override fun onPause() {
        ad_view.pause()
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

    fun showBookList(bookList: BookList) {
        // 书单对象只有这个用了，不需要存起来，
        title = bookList.name
    }

    private fun refresh() {
        srlRefresh.isRefreshing = true
        presenter.refresh()
    }

    /**
     * 强行刷新，重新下载小说详情，主要是看最新章，
     */
    private fun forceRefresh() {
        mAdapter.refresh()
        refresh()
    }

    fun showNovelList(list: List<Novel>) {
        mAdapter.data = list
        srlRefresh.isRefreshing = false
    }

    fun selectToAdd(list: List<Novel>, nameArray: Array<String>, containsArray: BooleanArray) {
        AlertDialog.Builder(this)
                .setTitle(R.string.contents)
                .setMultiChoiceItems(nameArray,
                        containsArray, { _, i, isChecked ->
                    if (isChecked) {
                        presenter.add(list[i])
                    } else {
                        presenter.remove(list[i])
                    }
                }).setCancelable(false)
                .setPositiveButton(android.R.string.yes) { _, _ ->
                    refresh()
                }
                .create().apply {
                    listView.isFastScrollEnabled = true
                }.show()
    }


    private fun add() {
        // 这些操作应该很块，不提示了，
        val list = listOf(R.string.bookshelf to {
            presenter.addFromBookshelf()
        }, R.string.history to {
            presenter.addFromHistory()
        })
        selector(getString(R.string.add_from), list.unzip().first.map { getString(it) }) { _, i ->
            list[i].second.invoke()
        }
    }

    private val snack: Snackbar by lazy {
        Snackbar.make(rvNovel, "", Snackbar.LENGTH_SHORT)
    }

    fun showBookListNotFound(message: String, e: Throwable) {
        // 不应该出现书单找不到的问题，
        finish()
        showError(message, e)
    }

    fun showError(message: String, e: Throwable) {
        srlRefresh.isRefreshing = false
        snack.setText(message + e.message)
        snack.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_book_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.add -> add()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}