package cc.aoeiuv020.panovel.download

import android.app.PendingIntent
import android.content.Context
import android.support.v4.app.NotificationCompat
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.main.MainActivity
import cc.aoeiuv020.panovel.util.NotifyLoopProxy
import org.jetbrains.anko.intentFor

/**
 * Created by AoEiuV020 on 2018.10.07-15:16:13.
 */
class DownloadingNotificationManager(
        private val ctx: Context,
        novel: Novel
) {

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

    // 进度百分比，
    fun progress(offset: Long, length: Long): Int = (if (length <= 0) {
        0f
    } else {
        offset.toFloat() / length
    } * 100).toInt()


    fun downloadStart(index: Int, name: String) {
        val offset = 0L
        val length = 0L
        val progress = progress(offset, length)
        nb.setContentText(ctx.getString(R.string.chapter_downloading_placeholder, index, name, offset, length))
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setProgress(100, progress, false)
        proxy.start(nb.build())
    }

    fun downloading(index: Int, name: String, offset: Long, length: Long) {
        // 更新数据，下次通知自己读取，
        val progress = progress(offset, length)
        nb.setContentText(ctx.getString(R.string.chapter_downloading_placeholder, index, name, offset, length))
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setProgress(100, progress, length <= 0)
        proxy.modify(nb.build())
    }

    fun downloadCompletion(index: Int, name: String) {
        nb.setContentText(ctx.getString(R.string.chapter_download_complete_placeholder, index, name))
                .setProgress(0, 0, false)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
        proxy.complete(nb.build())
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
