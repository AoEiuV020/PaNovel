package cc.aoeiuv020.panovel.donate

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.server.ServerManager
import cc.aoeiuv020.panovel.settings.AdSettings
import cc.aoeiuv020.panovel.util.hide
import kotlinx.android.synthetic.main.activity_donate.*
import org.jetbrains.anko.ctx
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
            Donate.paypal.pay(ctx)
        }

        lAlipay.setOnClickListener {
            Donate.alipay.pay(ctx)
        }

        lWeChatPay.setOnClickListener {
            Donate.weChatPay.pay(ctx)
        }

        val redPacketUrl = ServerManager.config?.redPacket
        if (redPacketUrl.isNullOrBlank()) {
            // 这样可以在服务器端控制这张图片的显示隐藏，
            ivRedPacket.hide()
        }
        ivRedPacket.setOnClickListener {
            Donate.alipay.red(ctx)
        }

        tvDonateExplain.text = assets.open("Donate.txt").reader().readText()
    }

    private var stopTime: Long = 0

    override fun onStart() {
        super.onStart()
        if (stopTime > 0 && System.currentTimeMillis() - stopTime > TimeUnit.SECONDS.toMillis(5)) {
            AdSettings.adEnabled = false
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
