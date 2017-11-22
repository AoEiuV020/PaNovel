package cc.aoeiuv020.panovel.history


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.base.item.BaseItemListView
import cc.aoeiuv020.panovel.base.item.DefaultItemListAdapter
import cc.aoeiuv020.panovel.main.MainActivity
import kotlinx.android.synthetic.main.novel_item_list.*


/**
 * 绝大部分照搬书架，
 * Created by AoEiuV020 on 2017.10.15-18:07:39.
 */
class HistoryFragment : Fragment(), BaseItemListView {
    private lateinit var mAdapter: DefaultItemListAdapter
    private lateinit var presenter: HistoryPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
            = inflater.inflate(R.layout.novel_item_list, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.setLayoutManager(LinearLayoutManager(context))
        presenter = HistoryPresenter()
        mAdapter = DefaultItemListAdapter(context, presenter)
        recyclerView.setAdapter(mAdapter)
        recyclerView.setRefreshAction {
            forceRefresh()
        }

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

    override fun showError(message: String, e: Throwable) {
        (activity as? MainActivity)?.showError(message, e)
    }
}