package cc.aoeiuv020.pager.test

import android.app.Activity
import android.os.Bundle
import cc.aoeiuv020.pager.AnimMode
import cc.aoeiuv020.pager.Pager
import cc.aoeiuv020.pager.animation.Margins

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val loader = NumberPagerDrawer(this)
        val pager = Pager(this).apply {
            drawer = loader
            animMode = AnimMode.SIMULATION
            margins = Margins(10, 20, 30, 40)
        }
        setContentView(pager)
    }
}
