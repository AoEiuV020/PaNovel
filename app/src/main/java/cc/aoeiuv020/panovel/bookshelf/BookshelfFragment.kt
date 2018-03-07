package cc.aoeiuv020.panovel.bookshelf

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.base.item.BaseItemListView
import cc.aoeiuv020.panovel.local.NovelHistory
import cc.aoeiuv020.panovel.local.Settings
import cc.aoeiuv020.panovel.main.MainActivity
import kotlinx.android.synthetic.main.novel_item_list.*

/**
 *
 * Created by AoEiuV020 on 2017.10.15-17:22:28.
 */
class BookshelfFragment : Fragment(), BaseItemListView {
    private lateinit var mAdapter: BookshelfItemListAdapter
    private val presenter: BookshelfPresenter = BookshelfPresenter()
    /**
     * 标记是否要强制刷新，
     * create后需要强制刷新，其他情况回来的不需要，
     * 强制刷新前会先验证Settings里的设置，
     */
    private var shouldForceRefresh = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
            = inflater.inflate(R.layout.novel_item_list, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.setLayoutManager(LinearLayoutManager(context))
        mAdapter = BookshelfItemListAdapter(context, presenter)
        recyclerView.setAdapter(mAdapter)
        recyclerView.setRefreshAction {
            forceRefresh()
        }

        shouldForceRefresh = true

        presenter.attach(this)
    }

    override fun onDestroyView() {
        presenter.detach()
        super.onDestroyView()
    }


    override fun onStart() {
        super.onStart()
        if (shouldForceRefresh && Settings.bookshelfAutoRefresh) {
            forceRefresh()
        } else {
            refresh()
        }
        shouldForceRefresh = false
    }

    fun refresh() {
        recyclerView.showSwipeRefresh()
        presenter.refresh()
    }

    /**
     * 强行刷新，重新下载小说详情，主要是看最新章，
     */
    private fun forceRefresh() {
        presenter.forceRefresh()
    }

    fun showNovelList(list: List<NovelHistory>) {
        mAdapter.data = list
        recyclerView.dismissSwipeRefresh()
        recyclerView.showNoMore()
    }

    override fun showError(message: String, e: Throwable) {
        (activity as? MainActivity)?.showError(message, e)
    }
}