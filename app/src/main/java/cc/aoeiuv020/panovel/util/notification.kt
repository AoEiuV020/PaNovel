package cc.aoeiuv020.panovel.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import cc.aoeiuv020.panovel.BuildConfig
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.main.MainActivity
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.intentFor
import java.util.concurrent.TimeUnit

/**
 * Created by AoEiuV020 on 2018.10.06-19:33:43.
 */

object NotificationChannelId {
    const val default = BuildConfig.APPLICATION_ID + ".default"
    const val update = BuildConfig.APPLICATION_ID + ".update"
    const val download = BuildConfig.APPLICATION_ID + ".download"
    const val downloading = BuildConfig.APPLICATION_ID + ".downloading"
    const val export = BuildConfig.APPLICATION_ID + ".export"
}

/**
 * 初始化项目中用到的通知渠道，
 */
fun Context.initNotificationChannel() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        return
    }
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(
            NotificationChannel(
                    NotificationChannelId.default,
                    getString(R.string.channel_name_default),
                    NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = getString(R.string.channel_description_default)
            }
    )
    notificationManager.createNotificationChannel(
            NotificationChannel(
                    NotificationChannelId.update,
                    getString(R.string.channel_name_update),
                    NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = getString(R.string.channel_description_update)
            }
    )
    notificationManager.createNotificationChannel(
            NotificationChannel(
                    NotificationChannelId.download,
                    getString(R.string.channel_name_download),
                    NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = getString(R.string.channel_description_download)
            }
    )
    notificationManager.createNotificationChannel(
            NotificationChannel(
                    NotificationChannelId.downloading,
                    getString(R.string.channel_name_downloading),
                    NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = getString(R.string.channel_description_downloading)
            }
    )
    notificationManager.createNotificationChannel(
            NotificationChannel(
                    NotificationChannelId.export,
                    getString(R.string.channel_name_export),
                    NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = getString(R.string.channel_description_export)
            }
    )
}

/**
 * 用来代理循环通知的情况，
 * 没有死循环，就算不主动结束，也会在delay时间后自动结束，
 */
class NotifyLoopProxy(
        ctx: Context,
        private val id: Int = (Math.random() * Int.MAX_VALUE).toInt(),
        // 最多delay毫秒一个通知，
        private val delay: Long = 300L
) : AnkoLogger {
    companion object {
        val DEFAULT_CANCEL_DELAY: Long = TimeUnit.SECONDS.toMillis(5)
    }

    private val handler = Handler(Looper.getMainLooper())

    // System services not available to Activities before onCreate()
    private val manager by lazy { NotificationManagerCompat.from(ctx) }

    private var waiting = false
    private var done = false
    private var canceled = false
    private var cancelDelay: Long = DEFAULT_CANCEL_DELAY
    private var mNotification: Notification? = null
    private val cancelBlock = Runnable {
        // 如果延时期间取消了取消，就不取消，
        if (canceled) {
            manager.cancel(id)
        }
    }
    private val loopBlock = Runnable {
        // 取出wrapper中的notification,
        if (mNotification != null) {
            notifyCached()
        }
        if (canceled) {
            handler.postDelayed(cancelBlock, cancelDelay)
        }
        // 执行完了取消等待状态，
        waiting = false
    }

    private val mainThread = Looper.getMainLooper().thread

    private fun runOnUiThread(block: () -> Unit) {
        if (mainThread == Thread.currentThread()) {
            block()
        } else {
            handler.post(block)
        }
    }

    private fun notifyCached() {
        val notification = mNotification
        // 置空，免得重复弹，
        mNotification = null
        notification?.let { n ->
            runOnUiThread {
                manager.notify(id, n)
            }
        }
    }

    fun start(notification: Notification) {
        done = false
        canceled = false
        handler.removeCallbacks(cancelBlock)
        handler.removeCallbacks(loopBlock)
        // 循环开始前先弹一次通知，
        // 之后隔delay时间弹一次，
        mNotification = notification
        notifyCached()
        waiting = true
        handler.postDelayed(loopBlock, delay)
    }

    fun modify(notification: Notification) {
        // 如果已经结束，无视modify, 不再弹通知，
        if (done) return
        // 如果正在等待状态，也就是loopBlock已经提交，还没执行，
        // 直接修改当前缓存的notification，
        // 不论当前是否已经存在notification, 只弹最后一个通知，跳过频率过高的通知，
        mNotification = notification
        if (!waiting) {
            notifyCached()
            waiting = true
            handler.postDelayed(loopBlock, delay)
        }
    }

    fun complete(notification: Notification) {
        done = true
        // 就算完成了，也等最后一个循环节走完，
        // 这里无视线程冲突，尽量都只用主线程，
        // 要是说刚好主线程正在进入loopBlock拿走notification,可能导致最后一个通知不是完成通知，
        mNotification = notification
        if (!waiting) {
            notifyCached()
        }
    }

    fun cancel(cancelDelay: Long = DEFAULT_CANCEL_DELAY) {
        if (canceled) {
            return
        }
        canceled = true
        this.cancelDelay = cancelDelay
        if (!waiting) {
            handler.postDelayed(cancelBlock, cancelDelay)
        }
    }

    fun error() {
        done = true
        waiting = false
        // 出错了直接停止循环，
        handler.removeCallbacks(loopBlock)
    }
}

fun Context.notify(id: Int, text: String? = null, title: String? = null, icon: Int = R.mipmap.ic_launcher_foreground, time: Long? = null, bigText: String? = null, channelId: String = NotificationChannelId.default) {
    val intent = intentFor<MainActivity>()
    val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

    val nb = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
    bigText?.let {
        nb.setStyle(NotificationCompat.BigTextStyle().bigText(it))
    }
    time?.let {
        nb.setWhen(it)
    }
    nb.apply {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            setLargeIcon(getBitmapFromVectorDrawable(icon))
            setSmallIcon(R.mipmap.ic_launcher_round)
        } else {
            setSmallIcon(icon)
        }
    }
    val manager = NotificationManagerCompat.from(this)
    manager.notify(id, nb.build())
}

fun Context.cancelNotify(id: Int) {
    val manager = NotificationManagerCompat.from(this)
    manager.cancel(id)
}

fun Context.cancelAllNotify() {
    val manager = NotificationManagerCompat.from(this)
    manager.cancelAll()
}
