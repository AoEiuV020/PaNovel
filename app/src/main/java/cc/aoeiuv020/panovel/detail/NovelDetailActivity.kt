@file:Suppress("DEPRECATION")

package cc.aoeiuv020.panovel.detail

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.GridLayoutManager
import android.view.Menu
import android.view.MenuItem
import cc.aoeiuv020.panovel.IView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.NovelDetail
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.local.Bookshelf
import cc.aoeiuv020.panovel.local.toBean
import cc.aoeiuv020.panovel.local.toJson
import cc.aoeiuv020.panovel.text.NovelTextActivity
import cc.aoeiuv020.panovel.util.alert
import cc.aoeiuv020.panovel.util.alertError
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_novel_detail.*
import kotlinx.android.synthetic.main.activity_novel_detail.view.*
import kotlinx.android.synthetic.main.content_novel_detail.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.browse
import org.jetbrains.anko.debug
import org.jetbrains.anko.startActivity

/**
 *
 * Created by AoEiuV020 on 2017.10.03-18:10:37.
 */
class NovelDetailActivity : AppCompatActivity(), IView, AnkoLogger {
    companion object {
        fun start(context: Context, novelItem: NovelItem) {
            context.startActivity<NovelDetailActivity>("novelItem" to novelItem.toJson())
        }
    }

    private lateinit var alertDialog: AlertDialog
    private lateinit var presenter: NovelDetailPresenter
    private lateinit var chapterAdapter: NovelChaptersAdapter
    private var novelDetail: NovelDetail? = null
    private lateinit var novelItem: NovelItem
    private var isRefreshEnable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        alertDialog = AlertDialog.Builder(this).create()

        // 低版本api(<=20)默认不能用矢量图的selector, 要这样设置，
        // it's not a BUG, it's a FEATURE,
        // https://issuetracker.google.com/issues/37100284
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        setContentView(R.layout.activity_novel_detail)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        novelItem = intent.getStringExtra("novelItem").toBean()
        val requester = novelItem.requester
        debug { "receive $requester" }

        chapterAdapter = NovelChaptersAdapter(this, novelItem)
        recyclerView.setAdapter(chapterAdapter)
        recyclerView.setLayoutManager(GridLayoutManager(this@NovelDetailActivity, 3))

        setTitle(novelItem)

        fabRead.setOnClickListener {
            novelDetail?.let {
                NovelTextActivity.start(this, it.novel)
            }
        }

        swipeRefreshLayout.setOnRefreshListener {
            refresh()
        }
        swipeRefreshLayout.setOnChildScrollUpCallback { _, _ -> !isRefreshEnable }
        app_bar.addOnOffsetChangedListener { _, verticalOffset ->
            isRefreshEnable = verticalOffset == 0
        }
        swipeRefreshLayout.isRefreshing = true

        presenter = NovelDetailPresenter(novelItem)
        presenter.attach(this)
        presenter.start()
    }

    override fun onDestroy() {
        presenter.detach()
        super.onDestroy()
    }

    override fun onRestart() {
        super.onRestart()
        chapterAdapter.refresh()
    }

    private fun setTitle(novelItem: NovelItem) {
        toolbar_layout.title = "${novelItem.name} - ${novelItem.author}"
    }

    fun showNovelDetail(detail: NovelDetail) {
        this.novelDetail = detail
        setTitle(detail.novel)
        fabStar.isChecked = Bookshelf.contains(detail)
        fabStar.setOnClickListener {
            fabStar.toggle()
            if (fabStar.isChecked) {
                Bookshelf.add(detail)
            } else {
                Bookshelf.remove(detail)
            }
        }
        // 有可能activity已经销毁，glide会报错，
        if (isDestroyed) return
        Glide.with(this).load(detail.bigImg).into(toolbar_layout.image)
        presenter.requestChapters(detail.requester)
    }

    fun showNovelChaptersDesc(chapters: ArrayList<NovelChapter>) {
        chapterAdapter.data = chapters
        recyclerView.recyclerView.post {
            swipeRefreshLayout.isRefreshing = false
        }
    }

    fun showError(message: String, e: Throwable) {
        swipeRefreshLayout.isRefreshing = false
        alertError(alertDialog, message, e)
    }

    private fun showNovelAbout() {
        novelDetail?.let {
            alert(alertDialog, it.introduction, "${it.novel.name} - ${it.novel.author}")
        }
    }

    private fun refresh() {
        swipeRefreshLayout.isRefreshing = true
        presenter.refresh()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.browse -> browse(novelItem.requester.url)
            R.id.info -> showNovelAbout()
            R.id.refresh -> refresh()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_detail, menu)
        return true
    }
}

