package cc.aoeiuv020.pager.test

import android.app.Activity
import android.os.Bundle
import cc.aoeiuv020.pager.AnimMode
import cc.aoeiuv020.pager.Pager
import cc.aoeiuv020.pager.animation.Margins
import org.jetbrains.anko.ctx

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val myDrawer = NumberPagerDrawer(ctx)
        setContentView(Pager(this).apply {
            drawer = myDrawer
            animMode = AnimMode.SIMULATION
            margins = Margins(10, 20, 30, 40)
        })
    }
}
