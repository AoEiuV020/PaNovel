package cc.aoeiuv020.panovel.download

import android.app.PendingIntent
import android.content.Context
import android.support.v4.app.NotificationCompat
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.main.MainActivity
import cc.aoeiuv020.panovel.settings.DownloadSettings
import cc.aoeiuv020.panovel.util.NotifyLoopProxy
import org.jetbrains.anko.intentFor

class DownloadNotificationManager(
        private val ctx: Context,
        novel: Novel
) {

    private val enable: Boolean get() = DownloadSettings.downloadProgress
    private val proxy: NotifyLoopProxy = NotifyLoopProxy(ctx)
    // 太早了Intent不能用，
    private val nb: NotificationCompat.Builder by lazy {
        val intent = ctx.intentFor<MainActivity>()
        val pendingIntent = PendingIntent.getActivity(ctx, 0, intent, 0)
        @Suppress("DEPRECATION")
        // 用过时的通知，可以兼容api26,
        val notificationBuilder = NotificationCompat.Builder(ctx)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)
                .setContentTitle(novel.name)
                .setContentIntent(pendingIntent)
        notificationBuilder.apply {
            setSmallIcon(android.R.drawable.stat_sys_download)
        }
        notificationBuilder
    }

    fun downloadStart(left: Int) {
        val exists = 0
        val downloads = 0
        val errors = 0
        val progress = ((exists + downloads + errors).toFloat() / ((exists + downloads + errors) + left) * 100).toInt()
        nb.setContentText(ctx.getString(R.string.downloading_placeholder, exists, downloads, errors, left))
                .setProgress(100, progress, false)
        if (enable) {
            proxy.start(nb.build())
        } else {
            // 以防万一通知开始了不结束，
            cancelNotification()
        }
    }

    fun downloading(exists: Int, downloads: Int, errors: Int, left: Int) {
        val progress = ((exists + downloads + errors).toFloat() / ((exists + downloads + errors) + left) * 100).toInt()
        nb.setContentText(ctx.getString(R.string.downloading_placeholder, exists, downloads, errors, left))
                .setProgress(100, progress, false)
        if (enable) {
            proxy.modify(nb.build())
        } else {
            // 以防万一通知开始了不结束，
            cancelNotification()
        }
    }

    fun downloadComplete(exists: Int, downloads: Int, errors: Int) {
        nb.setContentText(ctx.getString(R.string.download_complete_placeholder, exists, downloads, errors))
                .setProgress(0, 0, false)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
        if (enable) {
            proxy.complete(nb.build())
        } else {
            // 以防万一通知开始了不结束，
            cancelNotification()
        }
    }

    fun cancelNotification(cancelDelay: Long = NotifyLoopProxy.DEFAULT_CANCEL_DELAY) {
        proxy.cancel(cancelDelay)
    }

    @Suppress("UNUSED_PARAMETER")
    fun error(message: String, t: Throwable) {
        // 出意外了直接停止通知循环，
        proxy.error()
    }

}
