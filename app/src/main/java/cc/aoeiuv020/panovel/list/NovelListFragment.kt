@file:Suppress("UnusedImport", "DEPRECATION")

package cc.aoeiuv020.panovel.list

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cc.aoeiuv020.panovel.IView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelGenre
import cc.aoeiuv020.panovel.api.NovelListItem
import cc.aoeiuv020.panovel.main.MainActivity
import cc.aoeiuv020.panovel.util.*
import kotlinx.android.synthetic.main.content_main.*

/**
 *
 * Created by AoEiuV020 on 2017.10.02-21:49:45.
 */
class NovelListFragment : Fragment(), IView {
    private lateinit var alertDialog: AlertDialog
    private lateinit var progressDialog: ProgressDialog
    private lateinit var presenter: NovelListPresenter
    private lateinit var mAdapter: NovelListRecyclerAdapter
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.content_main, container, false)

        alertDialog = AlertDialog.Builder(context).create()
        progressDialog = ProgressDialog(context)

        mAdapter = NovelListRecyclerAdapter(context)

        presenter = NovelListPresenter()
        presenter.attach(this)
        return root
    }

    override fun onDestroyView() {
        presenter.detach()
        super.onDestroyView()
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        recyclerView.iAdapter = mAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    fun showError(message: String, e: Throwable) {
        progressDialog.dismiss()
        activity.alertError(alertDialog, message, e)
    }

    fun showNovelList(novelList: List<NovelListItem>) {
        progressDialog.dismiss()
        recyclerView.run {
            scrollToPosition(0)
            mAdapter.setData(novelList)
            setOnLoadMoreListener {
                presenter.loadNextPage()
            }
            setLoadMoreEnabled(true)
            loadMoreFooterView.show()
        }
    }

    fun addNovelList(novelList: List<NovelListItem>) {
        mAdapter.addAll(novelList)
    }

    fun showYetLastPage() {
        recyclerView.apply {
            setLoadMoreEnabled(false)
            loadMoreFooterView.hide()
        }
        progressDialog.dismiss()
        activity.alert(alertDialog, R.string.yet_last_page)
    }

    fun showGenre(genre: NovelGenre) {
        recyclerView.setLoadMoreEnabled(true)
        activity.loading(progressDialog, R.string.novel_list)
        presenter.requestNovelList(genre)
    }

    fun showUrl(url: String) {
        (activity as MainActivity).showUrl(url)
    }
}

