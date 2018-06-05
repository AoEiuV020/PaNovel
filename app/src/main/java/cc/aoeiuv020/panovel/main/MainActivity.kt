@file:Suppress("DEPRECATION")

package cc.aoeiuv020.panovel.main

import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import cc.aoeiuv020.panovel.App
import cc.aoeiuv020.panovel.BuildConfig
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.booklist.BookListFragment
import cc.aoeiuv020.panovel.bookshelf.BookshelfFragment
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.detail.NovelDetailActivity
import cc.aoeiuv020.panovel.donate.DonateActivity
import cc.aoeiuv020.panovel.export.ExportActivity
import cc.aoeiuv020.panovel.history.HistoryFragment
import cc.aoeiuv020.panovel.local.Check
import cc.aoeiuv020.panovel.local.DevMessage
import cc.aoeiuv020.panovel.migration.Migration
import cc.aoeiuv020.panovel.migration.MigrationPresenter
import cc.aoeiuv020.panovel.migration.MigrationView
import cc.aoeiuv020.panovel.open.OpenManager
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.search.SiteChooseActivity
import cc.aoeiuv020.panovel.settings.GeneralSettings
import cc.aoeiuv020.panovel.settings.SettingsActivity
import cc.aoeiuv020.panovel.util.VersionName
import cc.aoeiuv020.panovel.util.loading
import cc.aoeiuv020.panovel.util.show
import com.google.android.gms.ads.AdListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_editor.view.*
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView
import org.jetbrains.anko.*


/**
 *
 * Created by AoEiuV020 on 2017.10.15-15:53:19.
 */
class MainActivity : AppCompatActivity(), MigrationView, AnkoLogger {

    lateinit var progressDialog: ProgressDialog
    private var migratingDialog: ProgressDialog? = null
    private lateinit var bookshelfFragment: BookshelfFragment
    private lateinit var historyFragment: HistoryFragment
    lateinit var bookListFragment: BookListFragment

    private val openListener: OpenManager.OpenListener = object : OpenManager.OpenListener {
        override fun onNovelOpened(novel: Novel) {
            NovelDetailActivity.start(ctx, novel)
        }

        override fun onBookListReceived(count: Int) {
            progressDialog.dismiss()
            bookListFragment.refresh()
            showMessage("添加书单，共${count}本，")
        }

        override fun onError(message: String, e: Throwable) {
            showError(message, e)
        }

        override fun onLoading(status: String) {
            loading(progressDialog, status)
        }
    }

    private lateinit var migrationPresenter: MigrationPresenter

    override fun showDowngrade(from: VersionName, to: VersionName) {
        debug {
            "showDowngrade <${from.name} to ${to.name}>"
        }
        ctx.alert {
            title = getString(R.string.warning)
            message = getString(R.string.downgrade_warning_placeholder, from.name, to.name)
            okButton { }
        }.show()
    }

    override fun showUpgrading(from: VersionName, migration: Migration) {
        val to = migration.to
        debug {
            "showUpgrading <${from.name} to ${to.name}>"
        }
        (migratingDialog ?: ProgressDialog(ctx).also { migratingDialog = it }).apply {
            setTitle(getString(R.string.migrating_title))
            setMessage(getString(R.string.migrating_message_placeholder, from.name, to.name, migration.message))
            show()
        }
    }

    override fun showMigrateComplete(from: VersionName, to: VersionName) {
        debug {
            "showMigrateComplete,"
        }
        migratingDialog?.dismiss()
        migratingDialog = null

        // 版本迁移数据结束了再加载控件，
        // TODO: 专门开个Splash页面比较好，
        initWidget()
    }

    override fun showMigrateError(from: VersionName, migration: Migration) {
        val to = migration.to
        debug {
            "showMigrateError <${from.name} to ${to.name}>"
        }
        migratingDialog?.dismiss()
        migratingDialog = null
        ctx.alert {
            title = getString(R.string.migrate_error_title)
            message = getString(R.string.migrate_error_message_placeholder, from.name, to.name, migration.message)
            okButton { }
            neutralPressed(getString(R.string.ignore)) {
                migrationPresenter.ignoreMigration(migration)
            }
        }.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        progressDialog = ProgressDialog(this)
        if (!isTaskRoot) {
            // 避免多开，
            // TODO: 考虑弄个专门用来跳转的splash页面之类，
            finish()
            // 不初始化，否则可能触发异步调用，比如异步检查更新，回来想通知更新时activity已经不在了，会崩溃，
            // 要特别留意destroy有没有涉及到什么必须初始化的，
            return
        }

        migrationPresenter = MigrationPresenter(this).apply {
            attach(this@MainActivity)
            start()
        }

        if (!BuildConfig.DEBUG) {
            // 异步检查签名，
            Check.asyncCheckSignature(this)
        }

        // 异步检查是否有更新，
        Check.asyncCheckVersion(this)
        // 异步获取可能存在的, 我放在网上想推给用户的消息，
        DevMessage.asyncShowMessage(this)


        ad_view.adListener = object : AdListener() {
            override fun onAdLoaded() {
                ad_view.show()
            }
        }

        if (GeneralSettings.adEnabled) {
            ad_view.loadAd(App.adRequest)
        }

    }

    private fun initWidget() {
        bookshelfFragment = BookshelfFragment()
        historyFragment = HistoryFragment()
        bookListFragment = BookListFragment()

        initTab(R.string.bookshelf to bookshelfFragment,
                R.string.book_list to bookListFragment,
                R.string.history to historyFragment)


        container.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                // 切回书架时刷新一下，
                if (position == 0) {
                    bookshelfFragment.refresh()
                }
                if (position == 1) {
                    fab.show()
                } else {
                    fab.hide()
                }
            }

        })

        fab.setOnClickListener { _ ->
            bookListFragment.newBookList()
        }
    }

    private fun initTab(vararg pair: Pair<Int, Fragment>) {
        val (titleIdList, fragmentList) = pair.unzip()
        val pagerAdapter = object : FragmentPagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int): Fragment = fragmentList[position]

            override fun getCount(): Int = fragmentList.size

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                val fragment = super.instantiateItem(container, position)
                when (fragment) {
                    is BookshelfFragment -> bookshelfFragment = fragment
                    is BookListFragment -> bookListFragment = fragment
                    is HistoryFragment -> historyFragment = fragment
                }
                return fragment
            }

        }

        container.adapter = pagerAdapter

        val commonNavigator = CommonNavigator(this)
        val titleList = titleIdList.map {
            getString(it)
        }
        commonNavigator.adapter = object : CommonNavigatorAdapter() {

            override fun getCount(): Int = titleList.size

            override fun getTitleView(context: Context, index: Int): IPagerTitleView {
                val colorTransitionPagerTitleView = ColorTransitionPagerTitleView(context)
                colorTransitionPagerTitleView.normalColor = Color.GRAY
                colorTransitionPagerTitleView.selectedColor = Color.WHITE
                colorTransitionPagerTitleView.text = titleList[index]
                colorTransitionPagerTitleView.setOnClickListener { container.currentItem = index }
                return colorTransitionPagerTitleView
            }

            override fun getIndicator(context: Context): IPagerIndicator {
                val indicator = LinePagerIndicator(context)
                indicator.mode = LinePagerIndicator.MODE_EXACTLY
                indicator.setColors(Color.WHITE)
                return indicator
            }
        }
        magic_indicator.navigator = commonNavigator
        ViewPagerHelper.bind(magic_indicator, container)

    }

    override fun onPause() {
        ad_view.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        ad_view.resume()
    }

    override fun onDestroy() {
        ad_view.destroy()
        super.onDestroy()
    }

    private fun showExplain() {
        alert(assets.open("Explain.txt").reader().readText(), getString(R.string.explain)) {
            yesButton { }
        }.show()
    }

    private fun scan() {
        val intent = Intent("com.google.zxing.client.android.SCAN")
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE")
        try {
            startActivityForResult(intent, 0)
        } catch (_: ActivityNotFoundException) {
            toast("没安装zxing二维码扫描器，")
        } catch (_: SecurityException) {
            toast("没权限？这里是调用zxing扫码，")
        }
    }

    private fun open() {
        alert {
            titleResource = R.string.open
            val layout = View.inflate(this@MainActivity, R.layout.dialog_editor, null)
            customView = layout
            val etName = layout.editText
            yesButton {
                val url = etName.text.toString()
                if (url.isNotEmpty()) {
                    OpenManager.open(this@MainActivity, url, openListener)
                }
            }
        }.show()
    }

    private fun subscript() {
        doAsync({ e ->
            val message = "订阅书架的小说失败，"
            Reporter.post(message, e)
            error(message, e)
            showError(message, e)
        }) {
            // 有检索书架列表，所以必须异步，
            DataManager.resetSubscript()
        }
    }

    private fun export() {
        ExportActivity.start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        data?.extras?.getString("SCAN_RESULT")?.let {
            OpenManager.open(this, it, openListener)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> SettingsActivity.start(this)
            R.id.search -> SiteChooseActivity.start(this)
            R.id.scan -> scan()
            R.id.open -> open()
            R.id.subscript -> subscript()
            R.id.export -> export()
            R.id.donate -> DonateActivity.start(this)
            R.id.explain -> showExplain()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private val snack: Snackbar by lazy {
        Snackbar.make(fab, "", Snackbar.LENGTH_SHORT)
    }

    fun showMessage(message: String) {
        snack.setText(message)
        snack.show()
    }

    override fun showError(message: String, e: Throwable) {
        progressDialog.dismiss()
        snack.setText(message + e.message)
        snack.show()
    }
}
