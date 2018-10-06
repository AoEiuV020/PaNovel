package cc.aoeiuv020.panovel.util

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.main.MainActivity
import org.jetbrains.anko.intentFor

/**
 * Created by AoEiuV020 on 2018.10.06-19:33:43.
 */

/**
 * 用来代理循环通知的情况，
 * 没有死循环，就算不主动结束，也会在delay时间后自动结束，
 */
class NotifyLoopProxy(
        ctx: Context,
        private val id: Int = 1,
        // 最多delay毫秒一个通知，
        private val delay: Long = 300L
) {
    private val handler = Handler(Looper.getMainLooper())
    // System services not available to Activities before onCreate()
    private val manager by lazy { NotificationManagerCompat.from(ctx) }

    private var waiting = false
    private var done = false
    private val wrapper = NotificationWrapper()
    private var loopBlock = Runnable {
        // 取出wrapper中的notification,
        val notification = wrapper.notification
        wrapper.notification = null
        // 如果wrapper中存在notification，表示有通知要弹，
        if (notification != null) {
            manager.notify(id, notification)
        }
        // 执行完了取消等待状态，
        waiting = false
    }

    fun start(notification: Notification) {
        // 循环开始前先弹一次通知，
        // 之后隔delay时间弹一次，
        manager.notify(id, notification)
        waiting = true
        handler.postDelayed(loopBlock, delay)
    }

    fun modify(notification: Notification) {
        // 如果已经结束，无视modify, 不再弹通知，
        if (done) return
        // 如果正在等待状态，也就是loopBlock已经提交，还没执行，
        // 直接修改当前缓存的notification，
        // 不论当前是否已经存在notification, 只弹最后一个通知，跳过频率过高的通知，
        if (waiting) {
            wrapper.notification = notification
        } else {
            manager.notify(id, notification)
            waiting = true
            handler.postDelayed(loopBlock, delay)
        }
    }

    fun complete(notification: Notification) {
        done = true
        // 就算完成了，也等最后一个循环节走完，
        // 这里无视线程冲突，尽量都只用主线程，
        // 要是说刚好主线程正在进入loopBlock拿走notification,可能导致最后一个通知不是完成通知，
        if (waiting) {
            wrapper.notification = notification
        } else {
            manager.notify(id, notification)
        }
    }

    fun error() {
        done = true
        // 出错了直接停止循环，
        handler.removeCallbacks(loopBlock)
    }

    class NotificationWrapper(
            var notification: Notification? = null
    )
}

fun Context.notify(id: Int, text: String? = null, title: String? = null, icon: Int = R.mipmap.ic_launcher_foreground, time: Long? = null, bigText: String? = null) {
    val intent = intentFor<MainActivity>()
    val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
    @Suppress("DEPRECATION")
    val nb = NotificationCompat.Builder(this)
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
