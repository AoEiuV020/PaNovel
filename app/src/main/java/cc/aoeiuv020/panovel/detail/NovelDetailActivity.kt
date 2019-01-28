@file:Suppress("DEPRECATION")

package cc.aoeiuv020.panovel.detail

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import cc.aoeiuv020.panovel.IView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.share.Share
import cc.aoeiuv020.panovel.text.NovelTextActivity
import cc.aoeiuv020.panovel.util.alert
import cc.aoeiuv020.panovel.util.alertError
import cc.aoeiuv020.panovel.util.noCover
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.activity_novel_detail.*
import kotlinx.android.synthetic.main.activity_novel_detail.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.ctx
import org.jetbrains.anko.debug
import org.jetbrains.anko.startActivity

/**
 *
 * Created by AoEiuV020 on 2017.10.03-18:10:37.
 */
class NovelDetailActivity : AppCompatActivity(), IView, AnkoLogger {
    companion object {
        fun start(ctx: Context, novel: Novel) {
            ctx.startActivity<NovelDetailActivity>(Novel.KEY_ID to novel.nId)
        }
    }

    private lateinit var alertDialog: AlertDialog
    private lateinit var presenter: NovelDetailPresenter
    private var novel: Novel? = null
    private var isRefreshEnable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        alertDialog = AlertDialog.Builder(this).create()

        setContentView(R.layout.activity_novel_detail)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val id = intent?.getLongExtra(Novel.KEY_ID, -1L)
        debug { "receive id: $id" }
        if (id == null || id == -1L) {
            Reporter.unreachable()
            finish()
            return
        }


        toolbar_layout.title = id.toString()

        fabRead.setOnClickListener {
            NovelTextActivity.start(this, id)
        }

        srlRefresh.setOnRefreshListener {
            refresh()
        }
        // 拉到顶部才允许下拉刷新，
        // 为了支持内部嵌套列表，
        srlRefresh.setOnChildScrollUpCallback { _, _ -> !isRefreshEnable }
        app_bar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _: AppBarLayout, verticalOffset: Int ->
            isRefreshEnable = verticalOffset == 0
        })
        srlRefresh.isRefreshing = true

        presenter = NovelDetailPresenter(id)
        presenter.attach(this)
        presenter.start()
    }

    override fun onDestroy() {
        presenter.detach()
        super.onDestroy()
    }

    fun showNovelDetail(novel: Novel) {
        srlRefresh.isRefreshing = false
        this.novel = novel
        toolbar_layout.title = novel.name
        // TODO: 调整上半部分展示内容，作者名网站名什么都加上，
        // TODO: 下面考虑用viewPager两页实现简介和目录，
        tvIntroduction.text = novel.introduction
        if (novel.image == noCover) {
            toolbar_layout.image.setImageResource(R.mipmap.no_cover)
        } else {
            Glide.with(ctx.applicationContext)
                    .load(novel.image)
                    .apply(RequestOptions().apply {
                        error(R.mipmap.no_cover)
                    })
                    .into(toolbar_layout.image)
        }
        fabRead.setOnClickListener {
            NovelTextActivity.start(this, novel)
        }
        fabStar.isChecked = novel.bookshelf
        fabStar.setOnClickListener {
            fabStar.toggle()
            presenter.updateBookshelf(fabStar.isChecked)
        }
    }

    fun showError(message: String, e: Throwable? = null) {
        srlRefresh.isRefreshing = false
        if (e == null) {
            alert(alertDialog, message)
        } else {
            alertError(alertDialog, message, e)
        }
    }

    private fun refresh() {
        srlRefresh.isRefreshing = true
        presenter.refresh()
    }

    private fun share() {
        presenter.share()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.browse -> presenter.browse()
            R.id.refresh -> refresh()
            R.id.share -> share()
            android.R.id.home -> onBackPressed()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_detail, menu)
        return true
    }

    fun showSharedUrl(url: String, qrCode: String) {
        Share.alert(this, url, qrCode)
    }

}

