package cc.aoeiuv020.panovel.find.qidiantu.list

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import cc.aoeiuv020.panovel.IView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.detail.NovelDetailActivity
import cc.aoeiuv020.panovel.find.qidiantu.QidiantuActivity
import cc.aoeiuv020.panovel.settings.OtherSettings
import cc.aoeiuv020.panovel.util.safelyShow
import cc.aoeiuv020.regex.pick
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_qidiantu_list.*
import kotlinx.android.synthetic.main.activity_single_search.srlRefresh
import org.jetbrains.anko.*

/**
 * 起点图首订统计小说列表页，
 */
class QidiantuListActivity : AppCompatActivity(), IView, AnkoLogger {
    companion object {
        fun start(ctx: Context) {
            ctx.startActivity<QidiantuListActivity>()
        }

        fun start(ctx: Context, postUrl: String) {
            ctx.startActivity<QidiantuListActivity>(
                "postUrl" to postUrl
            )
        }
    }

    private lateinit var presenter: QidiantuListPresenter
    private lateinit var adapter: QidiantuListAdapter
    private lateinit var headAdapter: QidiantuPostAdapter
    private lateinit var postUrl: String
    private var itemJumpQidian: MenuItem? = null

    private val snack: Snackbar by lazy {
        Snackbar.make(rvContent, "", Snackbar.LENGTH_SHORT)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qidiantu_list)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setTitle(R.string.qidiantu)

        postUrl = intent.getStringExtra("postUrl") ?: "https://www.qidiantu.com/shouding/"

        srlRefresh.isRefreshing = true
        srlRefresh.setOnRefreshListener {
            presenter.refresh()
        }

        initRecycler()

        presenter = QidiantuListPresenter()
        presenter.attach(this)

        presenter.start(this, postUrl)
    }


    override fun onDestroy() {
        presenter.stop()
        super.onDestroy()
    }

    private fun initRecycler() {
        rvHead.adapter = QidiantuPostAdapter().also {
            headAdapter = it
            it.setOnItemClickListener(object : QidiantuPostAdapter.OnItemClickListener {
                override fun onItemClick(item: Post) {
                    start(ctx, item.url)
                    finish()
                }
            })
        }
        rvContent.adapter = QidiantuListAdapter().also {
            adapter = it
            it.setOnItemClickListener(object : QidiantuListAdapter.OnItemClickListener {
                override fun onItemClick(item: Item) {
                    try {
                        openBook(item)
                    } catch (ignore: Exception) {
                        innerBrowse(item.url)
                    }
                }
            })
        }
    }

    fun innerBrowse(url: String) {
        QidiantuActivity.start(this, url)
    }

    private fun openBook(item: Item) {
        // http://www.qidiantu.com/info/1030268248.html
        val bookId = item.url.pick("http.*/info/(\\d*)").first()
        // https://book.qidian.com/info/1027440366
        // https://m.qidian.com/info/1027440366
        if (OtherSettings.jumpQidian) {
            try {
                // I/ActivityTaskManager: START u0 {act=android.intent.action.VIEW cat=[android.intent.category.BROWSABLE] dat=QDReader://app/showBook?query={"bookId":1027440366} flg=0x14400000 cmp=com.qidian.QDReader/.ui.activity.MainGroupActivity (has extras)} from uid 10241
                // intent://app/showBook?query=%7B%22bookId%22%3A1027440366%7D#Intent;scheme=QDReader;S.browser_fallback_url=http%3A%2F%2Fdownload.qidian.com%2Fapknew%2Fsource%2FQDReaderAndroid.apk;end
                val intent = Intent.parseUri(
                    "intent://app/showBook?query=%7B%22bookId%22%3A$bookId%7D#Intent;scheme=QDReader;S.browser_fallback_url=http%3A%2F%2Fdownload.qidian.com%2Fapknew%2Fsource%2FQDReaderAndroid.apk;end",
                    Intent.URI_INTENT_SCHEME
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                return
            } catch (e: ActivityNotFoundException) {
                toast(R.string.qidian_not_found)
                OtherSettings.jumpQidian = false
                updateItem()
            }
        }
        presenter.open(item, bookId)
    }

    fun openNovelDetail(novel: Novel) {
        NovelDetailActivity.start(ctx, novel)
    }

    fun showResult(data: List<Item>, head: List<Post>, title: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && isDestroyed) {
            return
        }
        srlRefresh.isRefreshing = false
        adapter.setData(data)
        headAdapter.setData(head)
        if (title.isNotBlank()) {
            setTitle(title)
        }
        if (data.isEmpty()) {
            snack.setText(R.string.qidiantu_empty_new)
            snack.show()
        } else {
            snack.dismiss()
        }
    }

    fun showProgress(retry: Int, maxRetry: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && isDestroyed) {
            return
        }
        srlRefresh.isRefreshing = false
        snack.setText(getString(R.string.qidianshuju_post_progress_place_holder, retry, maxRetry))
        snack.show()
    }

    fun showError(message: String, e: Throwable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && isDestroyed) {
            return
        }
        srlRefresh.isRefreshing = false
        snack.dismiss()
        alert(
            title = ctx.getString(R.string.error),
            message = message + e.message
        ) {
            okButton { }
        }.safelyShow()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_qidianshuju_list, menu)
        itemJumpQidian = menu.findItem(R.id.qidian)
        updateItem()
        return true
    }

    override fun onRestart() {
        super.onRestart()
        updateItem()
    }

    private fun updateItem() {
        itemJumpQidian?.setIcon(
            if (OtherSettings.jumpQidian) {
                R.drawable.ic_jump_qidian
            } else {
                R.drawable.ic_jump_qidian_blocked
            }
        )
    }

    private fun toggleQidian() {
        OtherSettings.jumpQidian = !OtherSettings.jumpQidian
        updateItem()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.browse -> presenter.browse()
            R.id.qidian -> toggleQidian()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
