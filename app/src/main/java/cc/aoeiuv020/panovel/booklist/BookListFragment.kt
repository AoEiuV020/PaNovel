@file:Suppress("DEPRECATION")

package cc.aoeiuv020.panovel.booklist

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cc.aoeiuv020.panovel.IView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.local.BookList
import cc.aoeiuv020.panovel.local.BookListData
import cc.aoeiuv020.panovel.main.MainActivity
import cc.aoeiuv020.panovel.util.loading
import cc.aoeiuv020.panovel.util.showKeyboard
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.content_book_list.*
import kotlinx.android.synthetic.main.dialog_editor.view.*
import kotlinx.android.synthetic.main.dialog_shared.view.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.browse
import org.jetbrains.anko.yesButton

/**
 *
 * Created by AoEiuV020 on 2017.11.22-14:07:56.
 */
class BookListFragment : Fragment(), IView {
    private lateinit var progressDialog: ProgressDialog
    private lateinit var mAdapter: BookListFragmentAdapter
    private val presenter: BookListFragmentPresenter = BookListFragmentPresenter()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.novel_item_list, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        progressDialog = ProgressDialog(context)
        recyclerView.setLayoutManager(LinearLayoutManager(context))
        mAdapter = BookListFragmentAdapter(context, presenter)
        recyclerView.setAdapter(mAdapter)
        recyclerView.setRefreshAction {
            refresh()
        }

        recyclerView.showSwipeRefresh()
        presenter.attach(this)
    }

    override fun onDestroyView() {
        presenter.detach()
        super.onDestroyView()
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

    fun showUploading() {
        context.loading(progressDialog, getString(R.string.uploading))
    }

    fun showSharedUrl(url: String, qrCode: String) {
        progressDialog.dismiss()
        val layout = View.inflate(context, R.layout.dialog_shared, null)
        layout.tvUrl.apply {
            text = url
            setTextIsSelectable(true)
            setOnClickListener {
                context.browse(url)
            }
        }
        Glide.with(this).load(qrCode).into(layout.ivQrCode)
        context.alert {
            titleResource = R.string.share
            customView = layout
            yesButton { }
        }.show()
    }

    fun newBookList() {
        context.alert {
            titleResource = R.string.add_book_list
            val layout = View.inflate(context, R.layout.dialog_editor, null)
            customView = layout
            val etName = layout.editText
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
        progressDialog.dismiss()
        (activity as? MainActivity)?.showError(message, e)
    }
}