package cc.aoeiuv020.panovel.booklist

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import cc.aoeiuv020.panovel.IView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.local.BookList
import cc.aoeiuv020.panovel.local.BookListData
import cc.aoeiuv020.panovel.main.MainActivity
import cc.aoeiuv020.panovel.util.showKeyboard
import kotlinx.android.synthetic.main.content_book_list.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.yesButton

/**
 *
 * Created by AoEiuV020 on 2017.11.22-14:07:56.
 */
class BookListFragment : Fragment(), IView {
    private lateinit var mAdapter: BookListFragmentAdapter
    private lateinit var presenter: BookListFragmentPresenter
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.novel_item_list, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {

        recyclerView.setLayoutManager(LinearLayoutManager(context))
        presenter = BookListFragmentPresenter()
        mAdapter = BookListFragmentAdapter(context, presenter)
        recyclerView.setAdapter(mAdapter)
        recyclerView.setRefreshAction {
            refresh()
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

    fun refresh() {
        recyclerView.showSwipeRefresh()
        presenter.refresh()
    }

    fun showBookListList(list: List<BookListData>) {
        mAdapter.data = list
        recyclerView.dismissSwipeRefresh()
        recyclerView.showNoMore()
    }

    fun newBookList() {
        context.alert {
            title = "添加书单"
            val etName = EditText(context)
            customView = etName
            yesButton {
                val name = etName.text.toString()
                if (name.isNotEmpty()) {
                    BookList.new(etName.text.toString())
                    refresh()
                }
            }
            etName.post { etName.showKeyboard() }
        }.show()
    }

    fun showError(message: String, e: Throwable) {
        (activity as? MainActivity)?.showError(message, e)
    }
}