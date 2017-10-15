package cc.aoeiuv020.panovel.main

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import cc.aoeiuv020.panovel.bookshelf.BookshelfFragment

class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            1 -> BookshelfFragment.newInstance()
            else -> BookshelfFragment.newInstance()
        }
    }

    override fun getCount(): Int {
        return 2
    }
}