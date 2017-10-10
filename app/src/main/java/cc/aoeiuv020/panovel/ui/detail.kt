@file:Suppress("DEPRECATION")

package cc.aoeiuv020.panovel.ui

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.NovelDetail
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.local.Bookshelf
import cc.aoeiuv020.panovel.local.toBean
import cc.aoeiuv020.panovel.local.toJson
import cc.aoeiuv020.panovel.presenter.NovelDetailPresenter
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_novel_detail.*
import kotlinx.android.synthetic.main.activity_novel_detail.view.*
import kotlinx.android.synthetic.main.content_novel_detail.*
import kotlinx.android.synthetic.main.novel_chapter_item.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.browse
import org.jetbrains.anko.debug
import org.jetbrains.anko.startActivity

/**
 *
 * Created by AoEiuV020 on 2017.10.03-18:10:37.
 */
class NovelDetailActivity : AppCompatActivity(), AnkoLogger {
    private val alertDialog: AlertDialog by lazy { AlertDialog.Builder(this).create() }
    private val progressDialog: ProgressDialog by lazy { ProgressDialog(this) }
    private lateinit var presenter: NovelDetailPresenter
    private lateinit var chapterAdapter: NovelChaptersAdapter
    private var novelDetail: NovelDetail? = null
    private lateinit var novelItem: NovelItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_novel_detail)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        novelItem = intent.getStringExtra("novelItem").toBean()
        val requester = novelItem.requester
        debug { "receive $requester" }

        recyclerView.adapter = NovelChaptersAdapter(this@NovelDetailActivity) { index ->
            novelDetail?.let {
                startActivity<NovelTextActivity>("novelItem" to it.novel.toJson(), "index" to index)
            }
        }.also { chapterAdapter = it }
        recyclerView.layoutManager = GridLayoutManager(this@NovelDetailActivity, 3)

        setTitle(novelItem)

        fabRead.setOnClickListener {
            novelDetail?.let {
                startActivity<NovelTextActivity>("novelItem" to it.novel.toJson())
            }
        }

        presenter = NovelDetailPresenter(this, requester)
        refresh()
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
        loading(progressDialog, R.string.novel_chapters)
        presenter.requestChapters(detail.requester)
    }

    fun showNovelChapters(chapters: List<NovelChapter>) {
        progressDialog.dismiss()
        chapterAdapter.setChapters(chapters)
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

    private fun refresh() {
        loading(progressDialog, R.string.novel_detail)
        presenter.start()
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

class NovelChaptersAdapter(private val ctx: Context, val callback: (Int) -> Unit) : RecyclerView.Adapter<NovelChaptersAdapter.Holder>() {
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

    inner class Holder(val root: View) : RecyclerView.ViewHolder(root), AnkoLogger {
        init {
            root.setOnClickListener {
                // 传出话数的顺序索引，
                callback(issuesDesc.size - 1 - layoutPosition)
            }
        }
    }

    fun setChapters(chapters: List<NovelChapter>) {
        issuesDesc = chapters.asReversed()
        notifyDataSetChanged()
    }
}
