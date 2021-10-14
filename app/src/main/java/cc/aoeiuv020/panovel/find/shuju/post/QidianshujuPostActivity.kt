package cc.aoeiuv020.panovel.find.shuju.post

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import cc.aoeiuv020.panovel.IView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.find.shuju.QidianshujuActivity
import cc.aoeiuv020.panovel.util.safelyShow
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_qidianshuju_post.rvContent
import kotlinx.android.synthetic.main.activity_single_search.srlRefresh
import org.jetbrains.anko.*

/**
 * 起点数据首订统计帖子列表页，
 */
class QidianshujuPostActivity : AppCompatActivity(), IView, AnkoLogger {
    companion object {
        fun start(ctx: Context) {
            ctx.startActivity<QidianshujuPostActivity>()
        }
    }

    private lateinit var presenter: QidianshujuPostPresenter
    private lateinit var adapter: QidianshujuPostAdapter

    private val snack: Snackbar by lazy {
        Snackbar.make(rvContent, "", Snackbar.LENGTH_SHORT)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qidianshuju_post)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setTitle(R.string.title_qidianshuju_first_order)

        srlRefresh.isRefreshing = true
        srlRefresh.setOnRefreshListener {
            presenter.refresh()
        }

        initRecycler()

        presenter = QidianshujuPostPresenter()
        presenter.attach(this)

        presenter.start(this)
    }


    override fun onDestroy() {
        presenter.stop()
        super.onDestroy()
    }

    private fun initRecycler() {
        rvContent.adapter = QidianshujuPostAdapter().also {
            adapter = it
        }
    }

    fun innerBrowse(url: String) {
        QidianshujuActivity.start(this, url)
    }

    fun showResult(data: List<Post>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && isDestroyed) {
            return
        }
        srlRefresh.isRefreshing = false
        adapter.setData(data)
        snack.dismiss()
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
        menuInflater.inflate(R.menu.menu_qidianshuju_post, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.browse -> presenter.browse()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
