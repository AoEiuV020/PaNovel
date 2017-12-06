package cc.aoeiuv020.pager.test

import android.app.Activity
import android.os.Bundle
import android.view.View
import cc.aoeiuv020.pager.AnimMode
import cc.aoeiuv020.pager.Pager
import cc.aoeiuv020.pager.animation.Margins
import org.jetbrains.anko.ctx

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(Pager(this).apply {
            drawer = LayoutDrawer(View.inflate(ctx, R.layout.page_item, null))
            animMode = AnimMode.SIMULATION
            margins = Margins(10, 20, 30, 40)
        })
    }
}
