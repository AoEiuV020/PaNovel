@file:Suppress("UnusedImport", "DEPRECATION")

package cc.aoeiuv020.panovel.list

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cc.aoeiuv020.panovel.App
import cc.aoeiuv020.panovel.IView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelGenre
import cc.aoeiuv020.panovel.api.NovelListItem
import cc.aoeiuv020.panovel.bookstore.BookstoreActivity
import cc.aoeiuv020.panovel.local.Settings
import cc.aoeiuv020.panovel.util.alertError
import kotlinx.android.synthetic.main.content_bookstore.*

/**
 *
 * Created by AoEiuV020 on 2017.10.02-21:49:45.
 */
class NovelListFragment : Fragment(), IView {
    private lateinit var alertDialog: AlertDialog
    private val presenter: NovelListPresenter = NovelListPresenter()
    private lateinit var mAdapter: NovelListRecyclerAdapter
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.content_bookstore, container, false)

        alertDialog = AlertDialog.Builder(context).create()

        mAdapter = NovelListRecyclerAdapter(context)

        presenter.attach(this)
        return root
    }

    override fun onDestroyView() {
        ad_view.destroy()
        presenter.detach()
        super.onDestroyView()
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        recyclerView.apply {
            setAdapter(mAdapter)
            setLayoutManager(LinearLayoutManager(context))
            setLoadMoreAction {
                presenter.loadNextPage()
            }
        }

        if (Settings.adEnabled) {
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

    fun showError(message: String, e: Throwable) {
        recyclerView.dismissSwipeRefresh()
        activity.alertError(alertDialog, message, e)
    }

    fun showNovelList(novelList: List<NovelListItem>) {
        mAdapter.data = novelList
        if (novelList.isEmpty()) {
            showNoMore()
        } else {
            mAdapter.openLoadMore()
        }
        recyclerView.run {
            recyclerView.scrollToPosition(0)
            dismissSwipeRefresh()
        }
    }

    fun addNovelList(novelList: List<NovelListItem>) {
        mAdapter.addAll(novelList)
    }

    fun showNoMore() {
        recyclerView.showNoMore()
    }

    fun showGenre(genre: NovelGenre) {
        recyclerView.showSwipeRefresh()
        recyclerView.setRefreshAction {
            presenter.requestNovelList(genre)
        }
        presenter.requestNovelList(genre)
    }

    fun showUrl(url: String) {
        (activity as BookstoreActivity).showUrl(url)
    }
}

