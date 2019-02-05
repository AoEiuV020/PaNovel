@file:Suppress("DEPRECATION")

package cc.aoeiuv020.panovel.booklist

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cc.aoeiuv020.panovel.IView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.data.entity.BookList
import cc.aoeiuv020.panovel.main.MainActivity
import cc.aoeiuv020.panovel.share.Share
import cc.aoeiuv020.panovel.util.loading
import cc.aoeiuv020.panovel.util.notNullOrReport
import cc.aoeiuv020.panovel.util.safelyShow
import cc.aoeiuv020.panovel.util.showKeyboard
import kotlinx.android.synthetic.main.dialog_editor.view.*
import kotlinx.android.synthetic.main.novel_item_list.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.alert
import org.jetbrains.anko.selector
import org.jetbrains.anko.yesButton

/**
 *
 * Created by AoEiuV020 on 2017.11.22-14:07:56.
 */
class BookListFragment : androidx.fragment.app.Fragment(), IView, AnkoLogger {
    private lateinit var progressDialog: ProgressDialog
    private val itemListener: BookListFragmentAdapter.ItemListener = object : BookListFragmentAdapter.ItemListener {
        override fun onClick(vh: BookListFragmentAdapter.ViewHolder) {
            BookListActivity.start(requireContext(), vh.bookList.nId)
        }

        override fun onLongClick(vh: BookListFragmentAdapter.ViewHolder): Boolean {
            val list = listOf(R.string.remove to { remove(vh) },
                    R.string.rename to { rename(vh.bookList) },
                    R.string.share to { shareBookList(vh.bookList) },
                    R.string.add_bookshelf to { addBookshelf(vh.bookList) },
                    R.string.remove_bookshelf to { removeBookshelf(vh.bookList) })
            requireContext().selector(requireContext().getString(R.string.action), list.unzip().first.map {
                requireContext().getString(it)
            }) { _, i ->
                list[i].second.invoke()
            }
            return true
        }
    }

    fun showRemoveBookshelfComplete() {
        showComplete(requireContext().getString(R.string.remove_bookshelf_complete))
        (activity as? MainActivity)?.refreshBookshelf()
    }

    fun showRemoving() {
        requireContext().loading(progressDialog, R.string.removing_bookshelf)
    }

    private fun removeBookshelf(bookList: BookList) {
        presenter.removeBookshelf(bookList)
    }

    fun showAddBookshelfComplete() {
        showComplete(requireContext().getString(R.string.add_bookshelf_complete))
        (activity as? MainActivity)?.refreshBookshelf()
    }

    fun showAdding() {
        requireContext().loading(progressDialog, R.string.removing_bookshelf)
    }

    private fun addBookshelf(bookList: BookList) {
        presenter.addBookshelf(bookList)
    }

    private val mAdapter = BookListFragmentAdapter(itemListener)

    private fun remove(vh: BookListFragmentAdapter.ViewHolder) {
        presenter.remove(vh.bookList)
    }

    private fun rename(bookList: BookList) {
        requireContext().alert {
            titleResource = R.string.rename
            val layout = View.inflate(context, R.layout.dialog_editor, null)
            customView = layout
            val etName = layout.editText
            yesButton {
                val name = etName.text.toString()
                if (name.isNotEmpty()) {
                    presenter.renameBookList(bookList, name)
                } else {
                    // 改名为空的话直接无视，懒得报错了，
                }
            }
            etName.post { etName.showKeyboard() }
        }.safelyShow()
    }

    fun shareBookList(bookList: BookList) {
        presenter.shareBookList(bookList)
    }

    private val presenter: BookListFragmentPresenter = BookListFragmentPresenter()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.novel_item_list, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        progressDialog = ProgressDialog(context)
        // Note: 这里不是小说列表，固定用LinearLayoutManager，
        rvNovel.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        rvNovel.adapter = mAdapter
        srlRefresh.setOnRefreshListener {
            refresh()
        }
        presenter.attach(this)
    }

    override fun onDestroyView() {
        presenter.detach()
        super.onDestroyView()
    }

    override fun onStart() {
        super.onStart()
        // 开始查询书单列表，
        // 每次切到这个页面就刷新，
        refresh()
    }

    fun refresh() {
        srlRefresh.isRefreshing = true
        presenter.refresh()
    }

    fun showBookListList(list: List<BookList>) {
        srlRefresh.isRefreshing = false
        mAdapter.data = list
    }

    fun showUploading() {
        requireContext().loading(progressDialog, getString(R.string.uploading))
    }

    fun showSharedUrl(url: String, qrCode: String) {
        progressDialog.dismiss()
        Share.alert(context!!, url, qrCode)
    }

    fun newBookList() {
        activity.notNullOrReport().alert {
            titleResource = R.string.add_book_list
            val layout = View.inflate(context, R.layout.dialog_editor, null)
            customView = layout
            val etName = layout.editText
            yesButton {
                val name = etName.text.toString()
                if (name.isNotEmpty()) {
                    presenter.newBookList(etName.text.toString())
                }
            }
            etName.post { etName.showKeyboard() }
        }.safelyShow()
    }

    fun showComplete(message: String) {
        srlRefresh.isRefreshing = false
        progressDialog.dismiss()
        (activity as? MainActivity)?.showMessage(message)
    }

    fun showError(message: String, e: Throwable) {
        srlRefresh.isRefreshing = false
        progressDialog.dismiss()
        (activity as? MainActivity)?.showError(message, e)
    }
}