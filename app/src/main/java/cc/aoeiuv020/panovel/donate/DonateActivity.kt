package cc.aoeiuv020.panovel.donate

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.settings.GeneralSettings
import kotlinx.android.synthetic.main.activity_donate.*
import org.jetbrains.anko.startActivity
import java.util.concurrent.TimeUnit


class DonateActivity : AppCompatActivity() {
    companion object {
        fun start(context: Context) {
            context.startActivity<DonateActivity>()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donate)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        lPaypal.setOnClickListener {
            Donate.paypal.start(this@DonateActivity)
        }

        lAlipay.setOnClickListener {
            Donate.alipay.start(this@DonateActivity)
        }

        lWeChatPay.setOnClickListener {
            Donate.weChatPay.start(this@DonateActivity)
        }

        tvDonateExplain.text = assets.open("Donate.txt").reader().readText()
    }

    private var stopTime: Long = 0

    override fun onStart() {
        super.onStart()
        if (stopTime > 0 && System.currentTimeMillis() - stopTime > TimeUnit.SECONDS.toMillis(5)) {
            GeneralSettings.adEnabled = false
        }
    }

    override fun onStop() {
        super.onStop()
        stopTime = System.currentTimeMillis()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

}
