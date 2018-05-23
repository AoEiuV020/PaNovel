package cc.aoeiuv020.panovel.bookshelf

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cc.aoeiuv020.panovel.IView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.list.DefaultNovelItemActionListener
import cc.aoeiuv020.panovel.list.NovelListAdapter
import cc.aoeiuv020.panovel.local.Settings
import cc.aoeiuv020.panovel.main.MainActivity
import kotlinx.android.synthetic.main.novel_item_list.*

/**
 *
 * Created by AoEiuV020 on 2017.10.15-17:22:28.
 */
class BookshelfFragment : Fragment(), IView {
    private val itemListener = DefaultNovelItemActionListener { message, e ->
        showError(message, e)
    }
    // TODO: 要支持多种视图，
    private val mAdapter = NovelListAdapter(R.layout.novel_item_big, itemListener)
    private val presenter: BookshelfPresenter = BookshelfPresenter()
    /**
     * 标记是否要强制刷新，
     * create后需要强制刷新，其他情况回来的不需要，
     * 强制刷新前会先验证Settings里的设置，
     * 其实就是标记是否是刚启动状态，
     */
    private var shouldForceRefresh = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.novel_item_list, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        rvNovel.layoutManager = LinearLayoutManager(context)
        rvNovel.adapter = mAdapter
        srlRefresh.setOnRefreshListener {
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
        srlRefresh.isRefreshing = true
        presenter.refresh()
    }

    /**
     * 强行刷新，重新下载小说详情，主要是看最新章，
     */
    private fun forceRefresh() {
        mAdapter.refresh()
        refresh()
    }

    fun showNovelList(list: List<Novel>) {
        mAdapter.data = list
        srlRefresh.isRefreshing = false
    }

    fun showError(message: String, e: Throwable) {
        // 按理说到这里已经不会是正在刷新的状态了，
        srlRefresh.isRefreshing = false
        (activity as? MainActivity)?.showError(message, e)
    }
}