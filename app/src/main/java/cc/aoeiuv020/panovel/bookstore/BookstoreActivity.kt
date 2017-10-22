@file:Suppress("DEPRECATION")

package cc.aoeiuv020.panovel.bookstore

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuItem
import cc.aoeiuv020.panovel.IView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelGenre
import cc.aoeiuv020.panovel.api.NovelSite
import cc.aoeiuv020.panovel.detail.NovelDetailActivity
import cc.aoeiuv020.panovel.list.NovelListFragment
import cc.aoeiuv020.panovel.local.Bookshelf
import cc.aoeiuv020.panovel.local.History
import cc.aoeiuv020.panovel.util.alertError
import cc.aoeiuv020.panovel.util.loading
import com.bumptech.glide.Glide
import com.miguelcatalan.materialsearchview.MaterialSearchView
import kotlinx.android.synthetic.main.activity_bookstore.*
import kotlinx.android.synthetic.main.app_bar_bookstore.*
import kotlinx.android.synthetic.main.nav_header_bookstore.view.*
import org.jetbrains.anko.*

/**
 *
 * Created by AoEiuV020 on 2017.10.02-21:37:23.
 */
class BookstoreActivity : BookstoreBaseNavigationActivity(), IView, AnkoLogger {
    companion object {
        private val GROUP_ID: Int = 1
        fun start(context: Context) {
            context.startActivity<BookstoreActivity>()
        }
    }

    private lateinit var alertDialog: AlertDialog
    private lateinit var progressDialog: ProgressDialog
    private var url: String = "https://github.com/AoEiuV020/PaNovel"
    private lateinit var presenter: BookstorePresenter
    private lateinit var genres: List<NovelGenre>
    private var site: NovelSite? = null
    private var genre: NovelGenre? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        alertDialog = AlertDialog.Builder(this).create()
        progressDialog = ProgressDialog(this)

        searchView.setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                // 收起软键盘，
                searchView.hideKeyboard(searchView)
                site?.also {
                    loading(progressDialog, R.string.search_result)
                    presenter.search(it, query)
                } ?: run {
                    debug { "没有选择网站，先弹出网站选择，" }
                    presenter.requestSites()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = false

        })

        presenter = BookstorePresenter()
        presenter.attach(this)
        presenter.start()
    }

    override fun onDestroy() {
        presenter.detach()
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.browse -> browse(url)
            R.id.refresh -> refresh()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_bookstore, menu)
        val item = menu.findItem(R.id.action_search)
        searchView.setMenuItem(item)
        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.groupId) {
            GROUP_ID -> {
                showGenre(genres[item.order])
            }
            else -> when (item.itemId) {
                R.id.select_sites -> presenter.requestSites()
                R.id.bookshelf -> {
                    val list = Bookshelf.list()
                    selector(getString(R.string.bookshelf), list.map { "${it.name} - ${it.site}" }) { _, i ->
                        val novelItem = list[i]
                        NovelDetailActivity.start(this, novelItem)
                    }
                }
                R.id.history -> History.list().let { list ->
                    selector(getString(R.string.history), list.map { it.novel }.map { "${it.name} - ${it.site}" }) { _, i ->
                        val novelItem = list[i].novel
                        NovelDetailActivity.start(this, novelItem)
                    }
                }
            }
        }
        closeDrawer()
        return true
    }

    fun showUrl(url: String) {
        this.url = url
    }

    private fun refresh() {
        genre?.let { showGenre(it) }
    }

    fun showGenre(genre: NovelGenre) {
        this.genre = genre
        title = genre.name
        url = genre.requester.url
        progressDialog.dismiss()
        (fragment_container as NovelListFragment).showGenre(genre)
        closeDrawer()
    }

    fun showSites(sites: List<NovelSite>) {
        AlertDialog.Builder(this@BookstoreActivity).setAdapter(SiteListAdapter(this@BookstoreActivity, sites)) { _, index ->
            val site = sites[index]
            this.site = site
            debug { "选中网站：${site.name}，弹出侧栏，" }
            showSite(site)
        }.show()
    }

    fun showSite(site: NovelSite) {
        this.site = site
        url = site.baseUrl
        openDrawer()
        loading(progressDialog, R.string.genre_list)
        nav_view.getHeaderView(0).apply {
            selectedSiteName.text = site.name
            Glide.with(this).load(site.logo).into(selectedSiteLogo)
        }
        presenter.requestGenres(site)
    }

    fun showGenres(genres: List<NovelGenre>) {
        this.genres = genres
        progressDialog.dismiss()
        nav_view.menu.run {
            removeGroup(GROUP_ID)
            genres.forEachIndexed { index, (name) ->
                add(GROUP_ID, index, index, name)
            }
        }
    }

    fun showError(message: String, e: Throwable) {
        progressDialog.dismiss()
        alertError(alertDialog, message, e)
    }
}

