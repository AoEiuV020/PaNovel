package cc.aoeiuv020.panovel.booklist

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import cc.aoeiuv020.gson.toBean
import cc.aoeiuv020.gson.toJson
import cc.aoeiuv020.panovel.App
import cc.aoeiuv020.panovel.IView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.data.NovelManager
import cc.aoeiuv020.panovel.data.entity.BookList
import cc.aoeiuv020.panovel.list.NovelListAdapter
import cc.aoeiuv020.panovel.list.NovelViewHolder
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.settings.ListSettings
import cc.aoeiuv020.panovel.settings.ServerSettings
import cc.aoeiuv020.panovel.util.getStringExtra
import cc.aoeiuv020.panovel.util.safelyShow
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.novel_item_list.*
import org.jetbrains.anko.*

/**
 *
 * Created by AoEiuV020 on 2017.11.22-14:49:22.
 */
class BookListActivity : AppCompatActivity(), IView, AnkoLogger {
    companion object {
        fun start(context: Context, bookListId: Long) {
            // 统一转成json字符串，拿的时候不需要给默认值，
            // 拿不到就直接退出，
            context.startActivity<BookListActivity>("bookListId" to bookListId.toJson(App.gson))
        }
    }

    // onCreate里赋值，必须有值，
    private var bookListId: Long = -1
    private lateinit var presenter: BookListActivityPresenter

    private val novelListAdapter by lazy {
        NovelListAdapter(initItem = { vh ->
            // 长按弹出删除菜单，只要这个就够了，
            vh.itemView.setOnLongClickListener {
                onItemLongClick(vh)
            }
        }, onError = ::showError)
    }

    private fun onItemLongClick(vh: NovelViewHolder): Boolean {
        // 长按弹出删除菜单，只要这个就够了，
        // TODO: 改成支持滑动删除感觉更好，
        val list = listOf(R.string.remove to {
            presenter.remove(vh.novelManager)
            this@BookListActivity.novelListAdapter.remove(vh.layoutPosition)
        })
        selector(getString(R.string.action), list.unzip().first.map { getString(it) }) { _, i ->
            list[i].second.invoke()
        }
        return true
    }

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

        rvNovel.layoutManager = if (ListSettings.gridView) {
            androidx.recyclerview.widget.GridLayoutManager(ctx, if (ListSettings.largeView) 3 else 5)
        } else {
            androidx.recyclerview.widget.LinearLayoutManager(ctx)
        }
        presenter = BookListActivityPresenter(bookListId)
        rvNovel.adapter = novelListAdapter
        srlRefresh.setOnRefreshListener {
            forceRefresh()
        }

        presenter.attach(this)
        // 查询书单名，只用来改title, 没什么大用，
        presenter.start()
    }

    override fun onRestart() {
        // 阅读后回来时要刷新，
        refresh()
        super.onRestart()
    }

    override fun onDestroy() {
        if (::presenter.isInitialized) {
            presenter.detach()
        }
        super.onDestroy()
    }

    fun showBookList(bookList: BookList) {
        // 书单对象只有这个用了，不需要存起来，
        title = bookList.name
        // 确实找到书单了再刷新列表，
        refresh()
    }

    fun showBookListNotFound(message: String, e: Throwable) {
        // 不应该出现书单找不到的问题，
        showError(message, e)
        finish()
    }

    private fun refresh() {
        srlRefresh.isRefreshing = true
        presenter.refresh()
    }

    /**
     * 强行刷新，重新下载小说详情，主要是看最新章，
     */
    private fun forceRefresh() {
        novelListAdapter.refresh()
        refresh()
    }

    fun showNovelList(list: List<NovelManager>) {
        novelListAdapter.data = list
        if (ServerSettings.askUpdate) {
            presenter.askUpdate(list)
        } else {
            srlRefresh.isRefreshing = false
        }
    }

    fun showAskUpdateResult(hasUpdateList: List<Long>) {
        srlRefresh.isRefreshing = false
        // 就算是空列表也要传进去，更新一下刷新时间，
        // 空列表可能是因为连不上服务器，
        novelListAdapter.hasUpdate(hasUpdateList)
    }

    @Suppress("UNUSED_PARAMETER")
    fun askUpdateError(message: String, e: Throwable) {
        // 询问服务器更新出错不展示，
        srlRefresh.isRefreshing = false
    }

    fun selectToAdd(list: List<NovelManager>, nameArray: Array<String>, containsArray: BooleanArray) {
        AlertDialog.Builder(this)
                .setTitle(R.string.contents)
                .setMultiChoiceItems(nameArray, containsArray) { _, i, isChecked ->
                    if (isChecked) {
                        presenter.add(list[i])
                    } else {
                        presenter.remove(list[i])
                    }
                }.setCancelable(false)
                .setPositiveButton(android.R.string.yes) { _, _ ->
                    refresh()
                }
                .create().apply {
                    listView.isFastScrollEnabled = true
                }.safelyShow()
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