package cc.aoeiuv020.panovel.history


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cc.aoeiuv020.panovel.IView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.main.MainActivity
import kotlinx.android.synthetic.main.content_bookshelf.*


/**
 * 绝大部分照搬书架，
 * Created by AoEiuV020 on 2017.10.15-18:07:39.
 */
class HistoryFragment : Fragment(), IView {
    private lateinit var mAdapter: HistoryAdapter
    private lateinit var presenter: HistoryPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.content_bookshelf, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.setLayoutManager(LinearLayoutManager(context))
        presenter = HistoryPresenter()
        mAdapter = HistoryAdapter(context, presenter)
        recyclerView.setAdapter(mAdapter)
        recyclerView.setRefreshAction {
            forceRefresh()
        }

        recyclerView.showSwipeRefresh()
        presenter.attach(this)
    }

    override fun onDetach() {
        presenter.detach()
        super.onDetach()
    }

    override fun onStart() {
        super.onStart()
        refresh()
    }

    private fun refresh() {
        recyclerView.showSwipeRefresh()
        presenter.refresh()
    }

    /**
     * 强行刷新，重新下载小说详情，主要是看最新章，
     */
    private fun forceRefresh() {
        presenter.forceRefresh()
    }

    fun showNovelList(list: List<NovelItem>) {
        mAdapter.data = list
        recyclerView.dismissSwipeRefresh()
        recyclerView.showNoMore()
    }

    fun showError(message: String, e: Throwable) {
        (activity as? MainActivity)?.showError(message, e)
    }
}