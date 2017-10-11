@file:Suppress("UnusedImport", "DEPRECATION")

package cc.aoeiuv020.panovel.list

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import cc.aoeiuv020.panovel.IView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelGenre
import cc.aoeiuv020.panovel.api.NovelListItem
import cc.aoeiuv020.panovel.detail.NovelDetailActivity
import cc.aoeiuv020.panovel.local.toJson
import cc.aoeiuv020.panovel.main.MainActivity
import cc.aoeiuv020.panovel.util.alert
import cc.aoeiuv020.panovel.util.alertError
import cc.aoeiuv020.panovel.util.loading
import kotlinx.android.synthetic.main.content_main.*
import org.jetbrains.anko.startActivity

/**
 *
 * Created by AoEiuV020 on 2017.10.02-21:49:45.
 */
class NovelListFragment : Fragment(), IView {
    private lateinit var alertDialog: AlertDialog
    private lateinit var progressDialog: ProgressDialog
    private lateinit var presenter: NovelListPresenter
    private var isEnd = false
    private var isLoadingNextPage = false
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.content_main, container, false)

        alertDialog = AlertDialog.Builder(context).create()
        progressDialog = ProgressDialog(context)

        presenter = NovelListPresenter()
        presenter.attach(this)
        return root
    }

    override fun onDestroyView() {
        presenter.detach()
        super.onDestroyView()
    }

    fun showError(message: String, e: Throwable) {
        progressDialog.dismiss()
        activity.alertError(alertDialog, message, e)
    }

    fun showNovelList(novelList: List<NovelListItem>) {
        // 如果activity已经退出，下面的不执行，
        if (activity == null || activity.isDestroyed) {
            return
        }
        isLoadingNextPage = false
        progressDialog.dismiss()
        listView.run {
            adapter = NovelListAdapter(activity, novelList)
            setOnItemClickListener { _, _, position, _ ->
                val item = adapter.getItem(position) as NovelListItem
                context.startActivity<NovelDetailActivity>("novelItem" to item.novel.toJson())
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
                        activity.loading(progressDialog, R.string.next_page)
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
        activity.alert(alertDialog, R.string.yet_last_page)
    }

    fun showGenre(genre: NovelGenre) {
        isEnd = false
        isLoadingNextPage = false
        activity.loading(progressDialog, R.string.novel_list)
        presenter.requestNovelList(genre)
    }

    fun showUrl(url: String) {
        (activity as MainActivity).showUrl(url)
    }
}

