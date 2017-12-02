package cc.aoeiuv020.pager.test

import android.app.Activity
import android.os.Bundle
import cc.aoeiuv020.pager.AnimMode
import cc.aoeiuv020.pager.Pager
import cc.aoeiuv020.pager.animation.Margins

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val loader = NumberPagerDrawer()
        val view = Pager(this).apply {
            init(loader, AnimMode.SCROLL, Margins(10, 20, 30, 40))
        }
        setContentView(view)
    }
}
