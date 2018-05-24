package cc.aoeiuv020.panovel.search

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import cc.aoeiuv020.panovel.IView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.data.entity.Site
import kotlinx.android.synthetic.main.activity_site_choose.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class SiteChooseActivity : AppCompatActivity(), IView {
    companion object {
        fun start(ctx: Context) {
            ctx.startActivity<SiteChooseActivity>()
        }
    }

    private lateinit var presenter: SiteChoosePresenter

    private val itemListener = object : SiteListAdapter.ItemListener {
        override fun onEnabledChanged(site: Site, enabled: Boolean) {
            presenter.enabledChange(site, enabled)
        }

        override fun onSiteSelect(site: Site) {
            SingleSearchActivity.start(ctx, site.name)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_site_choose)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        rvSiteList.layoutManager = LinearLayoutManager(this)

        presenter = SiteChoosePresenter()
        presenter.attach(this)

        presenter.start()
    }

    fun showSiteList(siteList: List<Site>) {
        val adapter = SiteListAdapter(siteList, itemListener)
        rvSiteList.adapter = adapter
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_site_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search -> FuzzySearchActivity.start(ctx)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    fun showError(message: String, e: Throwable) {
        toast(message + e)
    }
}
