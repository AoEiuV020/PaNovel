@file:Suppress("UnusedImport", "DEPRECATION")

package cc.aoeiuv020.panovel.ui

import android.app.Activity
import android.app.ProgressDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelGenre
import cc.aoeiuv020.panovel.api.NovelListItem
import cc.aoeiuv020.panovel.presenter.NovelListPresenter
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.novel_list_item.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.startActivity

/**
 *
 * Created by AoEiuV020 on 2017.10.02-21:49:45.
 */
class NovelListFragment : Fragment() {
    private val alertDialog: AlertDialog by lazy { AlertDialog.Builder(context).create() }
    private val progressDialog: ProgressDialog by lazy { ProgressDialog(context) }
    private lateinit var presenter: NovelListPresenter
    private var isEnd = false
    private var isLoadingNextPage = false
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.content_main, container, false)
        presenter = NovelListPresenter(this)
        return root
    }

    fun showError(message: String, e: Throwable) {
        progressDialog.dismiss()
        context.alertError(alertDialog, message, e)
    }

    fun showNovelList(novelList: List<NovelListItem>) {
        isLoadingNextPage = false
        progressDialog.dismiss()
        listView.run {
            adapter = NovelListAdapter(activity, novelList)
            setOnItemClickListener { _, _, position, _ ->
                val item = adapter.getItem(position) as NovelListItem
                context.startActivity<NovelDetailActivity>("novelItem" to item.novel)
            }
            setOnScrollListener(object : AbsListView.OnScrollListener {
                private var lastItem = 0

                override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                    // 求画面上最后一个的索引，并不准，可能是最后一个+1,
                    lastItem = firstVisibleItem + visibleItemCount
                }

                override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {
                    // 差不多就好，反正没到底也快了，
                    if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                            && lastItem >= adapter.count - 2) {
                        if (isLoadingNextPage || isEnd) {
                            return
                        }
                        isLoadingNextPage = true
                        context.loading(progressDialog, R.string.next_page)
                        presenter.loadNextPage()
                    }
                }
            })
        }
    }

    fun addNovelList(novelList: List<NovelListItem>) {
        isLoadingNextPage = false
        progressDialog.dismiss()
        if (listView.adapter != null) {
            (listView.adapter as NovelListAdapter).addAll(novelList)
        } else {
            showNovelList(novelList)
        }
    }

    fun showYetLastPage() {
        isEnd = true
        isLoadingNextPage = false
        progressDialog.dismiss()
        context.alert(alertDialog, R.string.yet_last_page)
    }

    fun showGenre(genre: NovelGenre) {
        isEnd = false
        isLoadingNextPage = false
        context.loading(progressDialog, R.string.novel_list)
        presenter.requestNovelList(genre)
    }

    fun showUrl(url: String) {
        (activity as MainActivity).showUrl(url)
    }
}

class NovelListAdapter(private val ctx: Activity, data: List<NovelListItem>) : BaseAdapter(), AnkoLogger {
    private val items = data.toMutableList()
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View
            = (convertView ?: LayoutInflater.from(ctx).inflate(R.layout.novel_list_item, parent, false)).apply {
        val novel = getItem(position)
        novel_name.text = novel.novel.name
        novel_author.text = novel.novel.author
        novel_info.text = novel.info
    }

    override fun getItem(position: Int) = items[position]

    override fun getItemId(position: Int) = 0L

    override fun getCount() = items.size
    fun addAll(novelList: List<NovelListItem>) {
        items.addAll(novelList)
        notifyDataSetChanged()
    }
}
