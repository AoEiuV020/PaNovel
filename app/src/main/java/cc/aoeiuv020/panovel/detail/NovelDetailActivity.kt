@file:Suppress("DEPRECATION")

package cc.aoeiuv020.panovel.detail

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import cc.aoeiuv020.panovel.App
import cc.aoeiuv020.panovel.IView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.settings.GeneralSettings
import cc.aoeiuv020.panovel.share.Share
import cc.aoeiuv020.panovel.text.NovelTextActivity
import cc.aoeiuv020.panovel.util.alert
import cc.aoeiuv020.panovel.util.alertError
import cc.aoeiuv020.panovel.util.show
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdListener
import kotlinx.android.synthetic.main.activity_novel_detail.*
import kotlinx.android.synthetic.main.activity_novel_detail.view.*
import org.jetbrains.anko.AnkoLogger
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

/*
        chapterAdapter = NovelChaptersAdapter(this, novelItem)
        rvNovel.setAdapter(chapterAdapter)
        rvNovel.setLayoutManager(GridLayoutManager(this@NovelDetailActivity, 3))
*/

        title = id.toString()

        fabRead.setOnClickListener {
            NovelTextActivity.start(this, id)
        }
/*
        fabStar.isChecked = Bookshelf.contains(novelItem)
        fabStar.setOnClickListener {
            fabStar.toggle()
            if (fabStar.isChecked) {
                Bookshelf.add(novelItem)
            } else {
                Bookshelf.remove(novelItem)
            }
        }
*/

        srlRefresh.setOnRefreshListener {
            refresh()
        }
        // 拉到顶部才允许下拉刷新，
        // 为了支持内部嵌套列表，
        srlRefresh.setOnChildScrollUpCallback { _, _ -> !isRefreshEnable }
        app_bar.addOnOffsetChangedListener { _, verticalOffset ->
            isRefreshEnable = verticalOffset == 0
        }
        srlRefresh.isRefreshing = true

        presenter = NovelDetailPresenter(id)
        presenter.attach(this)
        presenter.start()

        ad_view.adListener = object : AdListener() {
            override fun onAdLoaded() {
                ad_view.show()
            }
        }

        if (GeneralSettings.adEnabled) {
            ad_view.loadAd(App.adRequest)
        }
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
        ad_view.destroy()
        presenter.detach()
        super.onDestroy()
    }

    override fun onRestart() {
        super.onRestart()
    }

    fun showNovelDetail(novel: Novel) {
        this.novel = novel
        title = novel.name
        // TODO: 调整上半部分展示内容，作者名网站名什么都加上，
        // TODO: 下面考虑用viewPager两页实现简介和目录，
        tvIntroduction.text = novel.introduction
        Glide.with(this).load(novel.image).into(toolbar_layout.image)
        fabRead.setOnClickListener {
            NovelTextActivity.start(this, novel)
        }
        fabStar.isChecked = novel.bookshelf
        fabStar.setOnClickListener {
            fabStar.toggle()
            novel.bookshelf = fabStar.isChecked
            presenter.updateBookshelf(novel)
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

