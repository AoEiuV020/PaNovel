package cc.aoeiuv020.panovel.bookstore

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import cc.aoeiuv020.panovel.R
import kotlinx.android.synthetic.main.activity_bookstore.*
import kotlinx.android.synthetic.main.app_bar_bookstore.*

/**
 * 抽屉Activity，绝大部分代码是自动生成的，
 * 分离出来仅供activity_main使用，
 * Created by AoEiuV020 on 2017.09.18-20:32:06.
 */
@Suppress("MemberVisibilityCanPrivate", "unused")
abstract class BookstoreBaseNavigationActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookstore)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
    }

    fun isDrawerOpen() = drawer_layout.isDrawerOpen(GravityCompat.START)

    fun closeDrawer() {
        drawer_layout.closeDrawer(GravityCompat.START)
    }

    fun openDrawer() {
        drawer_layout.openDrawer(GravityCompat.START)
    }

    override fun onBackPressed() {
        if (isDrawerOpen()) {
            closeDrawer()
        } else {
            if (searchView.isSearchOpen) {
                searchView.closeSearch()
            } else {
                super.onBackPressed()
            }
        }
    }
}
