package cc.aoeiuv020.panovel.util

import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.main.MainActivity
import org.jetbrains.anko.intentFor

/**
 * Created by AoEiuV020 on 2018.10.06-19:33:43.
 */

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
