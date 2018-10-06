package cc.aoeiuv020.panovel.download

import android.app.PendingIntent
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.data.entity.Novel
import org.jetbrains.anko.intentFor

class DownloadNotificationManager(
        ctx: Context,
        novel: Novel
) : DownloadListener {
    private val handler = Handler(Looper.getMainLooper())
    /**
     * 安卓通知是不按顺序的，使用唯一runnable确保顺序，
     */
    private val downloadingRunnable = object : Runnable {
        var exists = 0
        var downloads = 0
        var errors = 0
        var left = 0
        fun set(exists: Int, downloads: Int, errors: Int, left: Int) {
            this.exists = exists
            this.downloads = downloads
            this.errors = errors
            this.left = left
        }

        // 太早了Intent不能用，
        val nb: NotificationCompat.Builder by lazy {
            val intent = ctx.intentFor<DownloadActivity>()
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
        // System services not available to Activities before onCreate()
        val manager by lazy { NotificationManagerCompat.from(ctx) }

        fun complete() {
            // 完成时停止通知循环，
            // 可能正在通知？删除后又调用了延迟100ms的通知，覆盖了完成的通知，
            // 不对啊，这是在主线程的操作，只有一个线程，那就是其中一个通知在阻塞中，这里通知完了才到它，
            // 不管了，这多线程实在恶心，给个一秒的延迟，确保最后通知，
            handler.removeCallbacks(this)
            handler.postDelayed({
                nb.setContentText(ctx.getString(R.string.download_complete_placeholder, exists, downloads, errors))
                        .setProgress(0, 0, false)
                        .setSmallIcon(android.R.drawable.stat_sys_download_done)
                manager.notify(1, nb.build())
            }, 1000)
        }

        override fun run() {
            val progress = ((exists + downloads + errors).toFloat() / ((exists + downloads + errors) + left) * 100).toInt()
            nb.setContentText(ctx.getString(R.string.downloading_placeholder, exists, downloads, errors, left))
                    .setProgress(100, progress, false)
            manager.notify(1, nb.build())
            // 100ms通知一次，避免过快，
            handler.postDelayed(this, 100)
        }
    }

    fun showDownloadStart(left: Int) {
        downloadingRunnable.left = left
        // 开始通知循环，
        handler.post(downloadingRunnable)
    }

    fun showDownloadError() {
        // 出意外了直接停止通知循环，
        handler.removeCallbacks(downloadingRunnable)
    }

    fun showDownloading(exists: Int, downloads: Int, errors: Int, left: Int) {
        // 更新数据，下次通知自己读取，
        downloadingRunnable.set(exists, downloads, errors, left)
    }

    fun showDownloadComplete(exists: Int, downloads: Int, errors: Int) {
        downloadingRunnable.set(exists, downloads, errors, 0)
        downloadingRunnable.complete()
    }

    override fun downloadStart(count: Int) {
        showDownloadStart(count)
    }

    override fun downloading(exists: Int, downloads: Int, errors: Int, left: Int) {
        showDownloading(exists, downloads, errors, left)
    }

    override fun downloadCompletion(exists: Int, downloads: Int, errors: Int) {
        showDownloadComplete(exists, downloads, errors)
    }

    override fun error(message: String, t: Throwable) {
        showDownloadError()
    }

}
