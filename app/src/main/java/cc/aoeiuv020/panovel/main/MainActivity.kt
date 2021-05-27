@file:Suppress("DEPRECATION")

package cc.aoeiuv020.panovel.main

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.backup.BackupActivity
import cc.aoeiuv020.panovel.booklist.BookListFragment
import cc.aoeiuv020.panovel.bookshelf.BookshelfFragment
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.detail.NovelDetailActivity
import cc.aoeiuv020.panovel.donate.DonateActivity
import cc.aoeiuv020.panovel.history.HistoryFragment
import cc.aoeiuv020.panovel.migration.Migration
import cc.aoeiuv020.panovel.migration.MigrationPresenter
import cc.aoeiuv020.panovel.migration.MigrationView
import cc.aoeiuv020.panovel.open.OpenManager
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.search.FuzzySearchActivity
import cc.aoeiuv020.panovel.search.SiteChooseActivity
import cc.aoeiuv020.panovel.settings.InterfaceSettings
import cc.aoeiuv020.panovel.settings.SettingsActivity
import cc.aoeiuv020.panovel.shuju.QidianshujuActivity
import cc.aoeiuv020.panovel.util.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_editor.view.*
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.UIUtil
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
    private val bookshelfFragment: BookshelfFragment?
        get() = supportFragmentManager.fragments.firstOrNull { it is BookshelfFragment } as BookshelfFragment?
    private val bookListFragment: BookListFragment?
        get() = supportFragmentManager.fragments.firstOrNull { it is BookListFragment } as BookListFragment?
    private val historyFragment: HistoryFragment?
        get() = supportFragmentManager.fragments.firstOrNull { it is HistoryFragment } as HistoryFragment?

    private val openListener: OpenManager.OpenListener = object : OpenManager.OpenListener {
        override fun onOtherCase(str: String) {
            progressDialog.dismiss()
            // 打开的不是网址，就直接精确搜索，
            FuzzySearchActivity.start(ctx, str)
        }

        override fun onNovelOpened(novel: Novel) {
            progressDialog.dismiss()
            NovelDetailActivity.start(ctx, novel)
        }

        override fun onLocalNovelImported(novel: Novel) {
            progressDialog.dismiss()
            bookshelfFragment?.refresh()
            showMessage("导入小说<${novel.bookId}>")
        }

        override fun onBookListReceived(count: Int) {
            progressDialog.dismiss()
            bookListFragment?.refresh()
            showMessage("添加书单，共${count}本，")
        }

        override fun onError(message: String, e: Throwable) {
            progressDialog.dismiss()
            showError(message, e)
        }

        override fun onLoading(status: String) {
            loading(progressDialog, status)
        }
    }

    private lateinit var migrationPresenter: MigrationPresenter

    fun refreshBookshelf() {
        bookshelfFragment?.refresh()
    }

    override fun showDowngrade(from: VersionName, to: VersionName) {
        debug {
            "showDowngrade <${from.name} to ${to.name}>"
        }
        ctx.alert {
            title = getString(R.string.warning)
            message = getString(R.string.downgrade_warning_placeholder, from.name, to.name)
            okButton { }
        }.safelyShow()
    }

    override fun showUpgrading(from: VersionName, migration: Migration) {
        val to = migration.to
        debug {
            "showUpgrading <${from.name} to ${to.name}>"
        }
        (migratingDialog ?: ProgressDialog(ctx).also { migratingDialog = it }).apply {
            setTitle(getString(R.string.migrating_title))
            setMessage(getString(R.string.migrating_message_placeholder, from.name, to.name, migration.message))
            safelyShow()
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
        }.safelyShow()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        initNotificationChannel()

        progressDialog = ProgressDialog(this)

        migrationPresenter = MigrationPresenter(this).apply {
            attach(this@MainActivity)
            start()
        }
        checkEmpty()

        // 异步检查签名，
        Check.asyncCheckSignature(this)

        // 异步检查是否有更新，
        Check.asyncCheckVersion(this)
        // 异步获取可能存在的, 我放在网上想推给用户的消息，
        DevMessage.asyncShowMessage(this)
    }

    private fun checkEmpty() {
        doAsync({ t ->
            Reporter.unreachable(t)
        }) {
            if (DataManager.isEmpty()) {
                uiThread { ctx ->
                    AlertDialog.Builder(ctx)
                            .setMessage(getString(R.string.tip_data_empty))
                            .setPositiveButton(R.string.sImport) { _, _ ->
                                BackupActivity.start(ctx)
                            }
                            .setNeutralButton(R.string.search) { _, _ ->
                                FuzzySearchActivity.start(ctx, "异世界")
                            }
                            .show()
                }
            }
        }
    }

    private fun initWidget() {
        initTab(R.string.bookshelf to BookshelfFragment(),
                R.string.book_list to BookListFragment(),
                R.string.history to HistoryFragment())


        container.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                if (position == 1) {
                    fab.show()
                } else {
                    fab.hide()
                }
            }

        })

        fab.setOnClickListener { _ ->
            bookListFragment?.newBookList()
        }
    }

    private fun initTab(vararg pair: Pair<Int, androidx.fragment.app.Fragment>) {
        val (titleIdList, fragmentList) = pair.unzip()
        val pagerAdapter = object : androidx.fragment.app.FragmentPagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int): androidx.fragment.app.Fragment = fragmentList[position]

            override fun getCount(): Int = fragmentList.size
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
                indicator.lineWidth = UIUtil.dip2px(context, 10.0).toFloat()
                indicator.setColors(Color.WHITE)
                return indicator
            }
        }
        magic_indicator.navigator = commonNavigator
        val titleContainer = commonNavigator.titleContainer
        titleContainer.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
        titleContainer.dividerDrawable = object : ColorDrawable() {
            override fun getIntrinsicWidth(): Int {
                return dip(15)
            }
        }

        ViewPagerHelper.bind(magic_indicator, container)

        if (InterfaceSettings.tabGravityCenter) {
            llIndicator.gravity = Gravity.CENTER_HORIZONTAL
        }
    }

    override fun onResume() {
        super.onResume()
        // 回到主页时清空所有通知，包括小说更新通知和其他导出下载等通知，
        cancelAllNotify()
    }

    override fun onDestroy() {
        migratingDialog?.dismiss()
        if (::progressDialog.isInitialized) {
            progressDialog.dismiss()
        }
        super.onDestroy()
    }

    private fun showExplain() {
        alert(assets.open("Explain.txt").reader().readText(), getString(R.string.explain)) {
            yesButton { }
        }.safelyShow()
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

    private fun open() = alert {
        titleResource = R.string.open
        val layout = View.inflate(this@MainActivity, R.layout.dialog_editor, null)
        customView = layout
        val etName = layout.editText
        etName.hint = getString(R.string.main_open_hint)
        yesButton {
            val url = etName.text.toString()
            if (url.isNotEmpty()) {
                OpenManager.open(this@MainActivity, url, openListener)
            }
        }
        neutralPressed(R.string.local_novel) {
            // 调文件管理器选择小说，
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Intent(Intent.ACTION_OPEN_DOCUMENT)
            } else {
                Intent(Intent.ACTION_GET_CONTENT)
            }
            intent.type = "*/*"
            startActivityForResult(intent, 1)
        }
    }.safelyShow()

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

    private fun downloadAll() {
        ctx.doAsync({ e ->
            val message = "全部下载失败，"
            Reporter.post(message, e)
            error(message, e)
            ctx.runOnUiThread {
                showError(message, e)
            }
        }) {
            DataManager.downloadAll()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            0 -> data?.extras?.getString("SCAN_RESULT")?.let {
                OpenManager.open(this, it, openListener)
            }
            1 -> data?.data?.let { uri ->
                OpenManager.open(this, uri, openListener)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> SettingsActivity.start(this)
            R.id.search -> FuzzySearchActivity.start(this)
            R.id.scan -> scan()
            R.id.open -> open()
            R.id.subscript -> subscript()
            R.id.cacheAll -> downloadAll()
            R.id.source -> SiteChooseActivity.start(this)
            R.id.qidianshuju -> QidianshujuActivity.start(this)
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
