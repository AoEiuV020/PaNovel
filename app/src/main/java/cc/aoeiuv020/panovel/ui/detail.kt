@file:Suppress("DEPRECATION")

package cc.aoeiuv020.panovel.ui

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.NovelDetail
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.api.NovelListItem
import cc.aoeiuv020.panovel.presenter.NovelDetailPresenter
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_novel_detail.*
import kotlinx.android.synthetic.main.activity_novel_detail.view.*
import kotlinx.android.synthetic.main.content_novel_detail.*
import kotlinx.android.synthetic.main.novel_chapter_item.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.browse
import org.jetbrains.anko.debug

/**
 *
 * Created by AoEiuV020 on 2017.10.03-18:10:37.
 */
class NovelDetailActivity : AppCompatActivity(), AnkoLogger {
    private val alertDialog: AlertDialog by lazy { AlertDialog.Builder(this).create() }
    private val progressDialog: ProgressDialog by lazy { ProgressDialog(this) }
    private lateinit var novelUrl: String
    private lateinit var presenter: NovelDetailPresenter
    private var novelDetail: NovelDetail? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_novel_detail)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val novelListItem = intent.getSerializableExtra("item") as NovelListItem
        debug { "receive $novelListItem" }
        novelUrl = novelListItem.requester.url

        recyclerView.adapter = NovelDetailAdapter(this@NovelDetailActivity) { index ->
            //            startActivity<NovelPageActivity>("novelName" to novelName, "novelUrl" to novelUrl, "issueIndex" to index)
        }
        recyclerView.layoutManager = GridLayoutManager(this@NovelDetailActivity, 3)

        loading(progressDialog, R.string.novel_detail)
        setTitle(novelListItem.novel)

        fab.setOnClickListener {
            //            startActivity<NovelPageActivity>("novelName" to novelName, "novelUrl" to novelUrl)
        }

        presenter = NovelDetailPresenter(this, novelListItem)
        presenter.start()
    }

    private fun setTitle(novelItem: NovelItem) {
        toolbar_layout.title = "${novelItem.name} - ${novelItem.author}"
    }

    fun showNovelDetail(detail: NovelDetail) {
        this.novelDetail = detail
        progressDialog.dismiss()
        setTitle(detail.novel)
        // 有可能activity已经销毁，glide会报错，
        if (isDestroyed) return
        Glide.with(this).load(detail.bigImg).into(toolbar_layout.image)
        (recyclerView.adapter as NovelDetailAdapter).setDetail(detail)
    }

    fun showError(message: String, e: Throwable) {
        progressDialog.dismiss()
        alertError(alertDialog, message, e)
    }

    private fun showNovelAbout() {
        novelDetail?.let {
            alert(alertDialog, it.info, "${it.novel.name} - ${it.novel.author}")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_detail, menu)
        menu.findItem(R.id.browse).setOnMenuItemClickListener {
            browse(novelUrl)
        }
        menu.findItem(R.id.info).setOnMenuItemClickListener {
            showNovelAbout()
            true
        }
        return true
    }
}

class NovelDetailAdapter(val ctx: Context, val callback: (Int) -> Unit) : RecyclerView.Adapter<NovelDetailAdapter.Holder>() {
    private lateinit var detail: NovelDetail
    private var issuesDesc = emptyList<NovelChapter>()
    override fun getItemCount() = issuesDesc.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val issue = issuesDesc[position]
        holder.root.apply {
            name.text = issue.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int)
            = Holder(LayoutInflater.from(ctx).inflate(R.layout.novel_chapter_item, parent, false))

    fun setDetail(detail: NovelDetail) {
        this.detail = detail
        issuesDesc = detail.chaptersAsc.asReversed()
        notifyDataSetChanged()
    }

    inner class Holder(val root: View) : RecyclerView.ViewHolder(root), AnkoLogger {
        init {
            root.setOnClickListener {
                // 传出话数的顺序索引，
                callback(issuesDesc.size - 1 - layoutPosition)
            }
        }
    }
}
