package cc.aoeiuv020.panovel.donate

import android.annotation.SuppressLint
import android.content.*
import android.widget.ImageView
import androidx.core.content.ContextCompat.getSystemService
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.util.notNullOrReport
import cc.aoeiuv020.panovel.util.safelyShow
import org.jetbrains.anko.alert
import org.jetbrains.anko.browse
import org.jetbrains.anko.toast


/**
 *
 * Created by AoEiuV020 on 2017.11.25-12:55:16.
 */
sealed class Donate {
    companion object {
        val paypal = Paypal()
        val alipay = Alipay()
        val weChatPay = WeChatPay()
    }

    abstract fun pay(context: Context)

    class Paypal : Donate() {
        companion object {
            private val name = "AoEiuV020"
        }

        override fun pay(context: Context) {
            context.browse("https://www.paypal.me/$name")
        }
    }

    /**
     * https://github.com/didikee/AndroidDonate/blob/master/donate/src/main/java/android/didikee/donate/AlipayDonate.java
     */
    class Alipay : Donate() {
        companion object {
            private val payCode = "FKX01135QSJ7NYBPR0PK01"
            private val scanUri = "alipayqr://platformapi/startapp?saId=10000007"
            private val redCode = "685703214"
        }

        override fun pay(context: Context) {
            context.browse("https://QR.ALIPAY.COM/$payCode")
        }

        fun open(context: Context) {
            try {
                val packageManager = context.packageManager
                val intent = packageManager.getLaunchIntentForPackage("com.eg.android.AlipayGphone")
                context.startActivity(intent)
            } catch (e: Exception) {
                context.toast("你好像没有安装支付宝")
            }
        }

        @SuppressLint("SetTextI18n")
        fun red(context: Context) {
            context.alert {
                message = "打开支付宝首页搜“$redCode”领红包，\n或者截图到支付宝扫码"
                positiveButton("支付宝") {
                    open(context)
                }
                negativeButton(R.string.copy) {
                    val cm: ClipboardManager = getSystemService<ClipboardManager>(context, ClipboardManager::class.java).notNullOrReport()
                    cm.primaryClip = ClipData.newPlainText("alipayRedCode", redCode)
                    open(context)
                }
            }.safelyShow()
        }
    }

    /**
     * https://github.com/didikee/AndroidDonate/blob/master/donate/src/main/java/android/didikee/donate/WeiXinDonate.java
     */
    class WeChatPay : Donate() {
        companion object {
            private val qrcodeId = R.mipmap.qrcode_wechatpay
            private val TENCENT_PACKAGE_NAME = "com.tencent.mm"
            private val TENCENT_ACTIVITY_BIZSHORTCUT = "com.tencent.mm.action.BIZSHORTCUT"
            private val TENCENT_EXTRA_ACTIVITY_BIZSHORTCUT = "LauncherUI.From.Scaner.Shortcut"
        }

        override fun pay(context: Context) {
            context.alert {
                val ivQR = ImageView(context)
                ivQR.setImageResource(qrcodeId)
                customView = ivQR
                positiveButton(R.string.jump_to_we_chat) {
                    val intent = Intent(TENCENT_ACTIVITY_BIZSHORTCUT)
                    intent.`package` = TENCENT_PACKAGE_NAME
                    intent.putExtra(TENCENT_EXTRA_ACTIVITY_BIZSHORTCUT, true)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    try {
                        context.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        context.toast("你好像没有安装微信")
                    }
                }
            }.safelyShow()
        }
    }

}