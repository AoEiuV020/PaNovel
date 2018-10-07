package cc.aoeiuv020.panovel.download

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cc.aoeiuv020.panovel.IView
import cc.aoeiuv020.panovel.R
import org.jetbrains.anko.startActivity

class DownloadActivity : AppCompatActivity(), IView {
    companion object {
        fun start(ctx: Context) {
            ctx.startActivity<DownloadActivity>()
        }
    }

    private lateinit var presenter: DownloadPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)

        presenter = DownloadPresenter()
        presenter.attach(this)
        presenter.start()
    }
}
