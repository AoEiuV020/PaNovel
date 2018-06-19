package cc.aoeiuv020.panovel.local

import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.data.NovelManager
import cc.aoeiuv020.panovel.main.MainActivity
import cc.aoeiuv020.panovel.util.getBitmapFromVectorDrawable
import cc.aoeiuv020.panovel.util.notNullOrReport
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.runOnUiThread
import java.io.File

/**
 * Created by AoEiuV020 on 2018.05.28-18:53:01.
 */
class NovelExporter(
        private val type: LocalNovelType,
        private val file: File,
        private val progressCallback: (Int, Int) -> Unit
) : AnkoLogger {
    companion object {
        private const val TEXT_FOLDER = "Text"
        private const val EPUB_FOLDER = "Epub"

        fun export(ctx: Context, type: LocalNovelType, novelManager: NovelManager) {
            val novel = novelManager.novel
            val fileName = if (novel.site.startsWith(".")) {
                novel.run { "$name.$author.txt" }
            } else {
                novel.run { "$name.$author.$site.txt" }
            }
            // 尝试导出到sd卡，没有就导出到私有目录，虽然这样的导出好像没什么意义，
            val filesDir = ctx.getExternalFilesDir(null)
                    ?.apply { exists() || mkdirs() }
                    ?.takeIf { it.canWrite() }
                    ?: ctx.filesDir
            val file = filesDir
                    .resolve(when (type) {
                        LocalNovelType.TEXT -> NovelExporter.TEXT_FOLDER
                        LocalNovelType.EPUB -> NovelExporter.EPUB_FOLDER
                    }).apply { exists() || mkdirs() }
                    .resolve(fileName)
            var lastP = -1
            // 太早了Intent不能用，<-- 我也不知道这在说什么，
            val nb: NotificationCompat.Builder by lazy {
                val intent = ctx.intentFor<MainActivity>()
                val pendingIntent = PendingIntent.getActivity(ctx, 0, intent, 0)
                // 用过时的通知，可以兼容api26,
                @Suppress("DEPRECATION")
                val notificationBuilder = NotificationCompat.Builder(ctx)
                        .setOnlyAlertOnce(true)
                        .setAutoCancel(true)
                        .setContentTitle(ctx.getString(R.string.exporting_title_placeholder, novel.name))
                        .setContentIntent(pendingIntent)
                notificationBuilder.apply {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        setLargeIcon(ctx.getBitmapFromVectorDrawable(R.drawable.ic_file_download))
                        setSmallIcon(R.mipmap.ic_launcher_round)
                    } else {
                        setSmallIcon(R.drawable.ic_file_download)
                    }
                }
                notificationBuilder
            }
            // System services not available to Activities before onCreate()
            val manager by lazy { NotificationManagerCompat.from(ctx) }
            var isDone = false
            NovelExporter(LocalNovelType.TEXT, file) { current, total ->
                if (current == total) {
                    // 以防万一，多一个isDone判断避免结束会调顺序出问题时不能通知结束，
                    isDone = true
                }
                // 进度分成一百份，
                val max = 100
                val progress = (current.toFloat() / total * max).toInt()
                if (progress > lastP && !isDone) {
                    lastP = progress
                    ctx.runOnUiThread {
                        nb.setProgress(max, progress, false)
                        manager.notify(1, nb.build())
                    }
                }
                if (isDone) {
                    ctx.runOnUiThread {
                        nb.setContentTitle(ctx.getString(R.string.export_title_complete_placeholder, novel.name))
                        nb.setStyle(NotificationCompat.BigTextStyle().bigText(ctx.getString(R.string.export_complete_big_placeholder, file.path)))
                        nb.setProgress(max, max, false)
                        manager.notify(1, nb.build())
                    }
                }
            }.export(novelManager)
        }
    }

    fun export(novelManager: NovelManager) {
        val novel = novelManager.novel
        val info = LocalNovelInfo(
                author = novel.author,
                name = novel.name,
                image = novel.image,
                introduction = novel.introduction,
                chapters = novelManager.requestChapters(false).map { LocalNovelChapter(name = it.name, extra = it.extra) },
                requester = novel.chapters
        )
        val exporter = when (type) {
            LocalNovelType.TEXT -> TextExporter(file)
            LocalNovelType.EPUB -> TODO()
        }
        val chapters = novelManager.requestChapters(false).map {
            LocalNovelChapter(name = it.name, extra = it.extra)
        }
        val contentProvider = object : ContentProvider {
            val container = DataManager.novelContentsCached(novel)
            override fun getNovelContent(extra: String): List<String> {
                return if (container.contains(extra)) {
                    // 判断过章节存在了，这个必须非空，除非导出过程删除了缓存，
                    novelManager.getContent(extra).notNullOrReport()
                } else {
                    listOf()
                }
            }
        }
        exporter.export(info, chapters, contentProvider, progressCallback)
    }
}