package cc.aoeiuv020.panovel.download

import android.app.PendingIntent
import android.content.Context
import android.support.v4.app.NotificationCompat
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.main.MainActivity
import cc.aoeiuv020.panovel.settings.DownloadSettings
import cc.aoeiuv020.panovel.util.NotifyLoopProxy
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.intentFor

/**
 * Created by AoEiuV020 on 2018.10.07-15:16:13.
 */
class DownloadingNotificationManager(
        private val ctx: Context
) : AnkoLogger {

    private val enable: Boolean get() = DownloadSettings.downloadThreadPregress
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
                .setContentIntent(pendingIntent)
        notificationBuilder.apply {
            setSmallIcon(android.R.drawable.stat_sys_download)
        }
        notificationBuilder
    }

    // 进度百分比，
    private fun progress(offset: Long, length: Long): Int = (if (length <= 0) {
        0f
    } else {
        offset.toFloat() / length
    } * 100).toInt()


    fun downloadStart(novel: Novel, index: Int, name: String) {
        nb.setContentTitle(novel.name)
        val offset = 0L
        val length = 0L
        val progress = progress(offset, length)
        nb.setContentText(ctx.getString(R.string.chapter_downloading_placeholder, index, name, offset, length))
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setProgress(100, progress, false)
        if (enable) {
            proxy.start(nb.build())
        } else {
            // 以防万一通知开始了不结束，
            cancelNotification()
        }
    }

    fun downloading(index: Int, name: String, offset: Long, length: Long) {
        // 更新数据，下次通知自己读取，
        val progress = progress(offset, length)
        nb.setContentText(ctx.getString(R.string.chapter_downloading_placeholder, index, name, offset, length))
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setProgress(100, progress, length <= 0)
        if (enable) {
            proxy.modify(nb.build())
        }
    }

    fun downloadComplete(index: Int, name: String) {
        nb.setContentText(ctx.getString(R.string.chapter_download_complete_placeholder, index, name))
                .setProgress(0, 0, false)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
        if (enable) {
            proxy.complete(nb.build())
        } else {
            cancelNotification()
        }
    }

    fun downloadError(index: Int, name: String, message: String) {
        nb.setContentText(ctx.getString(R.string.chapter_download_error_placeholder, index, name, message))
                .setProgress(0, 0, false)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
        if (enable) {
            proxy.complete(nb.build())
        } else {
            cancelNotification()
        }
    }

    fun cancelNotification(cancelDelay: Long = NotifyLoopProxy.DEFAULT_CANCEL_DELAY) {
        proxy.cancel(cancelDelay)
    }

}
