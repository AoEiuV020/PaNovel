package cc.aoeiuv020.panovel.local

import android.app.PendingIntent
import android.content.Context
import android.support.v4.app.NotificationCompat
import cc.aoeiuv020.panovel.App
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.data.NovelManager
import cc.aoeiuv020.panovel.main.MainActivity
import cc.aoeiuv020.panovel.util.NotifyLoopProxy
import cc.aoeiuv020.panovel.util.notNullOrReport
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.runOnUiThread
import java.io.File
import java.io.InputStream
import java.net.URL

/**
 * Created by AoEiuV020 on 2018.05.28-18:53:01.
 */
class NovelExporter(
        private val type: LocalNovelType,
        private val file: File,
        private val progressCallback: (Int, Int) -> Unit
) : AnkoLogger {
    companion object : AnkoLogger {
        private const val TEXT_FOLDER = "Text"
        private const val EPUB_FOLDER = "Epub"

        override val loggerTag: String
            get() = "NovelExporter"

        fun export(ctx: Context, type: LocalNovelType, novelManager: NovelManager) {
            val novel = novelManager.novel
            // 本地小说的site就是后缀，不要重复了，
            val fileName = if (novel.site.startsWith(".")) {
                novel.run { "$name.$author${type.suffix}" }
            } else {
                novel.run { "$name.$author.$site${type.suffix}" }
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
                    setSmallIcon(android.R.drawable.stat_sys_download)
                }
                notificationBuilder
            }
            val proxy = NotifyLoopProxy(ctx)
            ctx.runOnUiThread {
                nb.setProgress(0, 0, true)
                proxy.start(nb.build())
            }
            NovelExporter(type, file) { current, total ->
                debug { "exporting $current/$total" }
                ctx.runOnUiThread {
                    if (current == total) {
                        nb.setContentTitle(ctx.getString(R.string.export_title_complete_placeholder, novel.name))
                        nb.setStyle(NotificationCompat.BigTextStyle().bigText(ctx.getString(R.string.export_complete_big_placeholder, file.path)))
                        nb.setProgress(total, current, false)
                        nb.setSmallIcon(android.R.drawable.stat_sys_download_done)
                        proxy.complete(nb.build())
                    } else {
                        nb.setProgress(total, current, false)
                        proxy.modify(nb.build())
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
                chapters = novelManager.requestChapters(false).map {
                    LocalNovelChapter(name = it.name, extra = it.extra)
                },
                requester = novel.chapters
        )
        val exporter = when (type) {
            LocalNovelType.TEXT -> TextExporter(file)
            LocalNovelType.EPUB -> EpubExporter(file)
        }
        val contentProvider = object : ContentProvider {
            val container = DataManager.novelContentsCached(novel)
            override fun getNovelContent(chapter: LocalNovelChapter): List<String> {
                return if (container.contains(chapter.extra)) {
                    // 判断过章节存在了，这个必须非空，除非导出过程删除了缓存，
                    novelManager.getContent(chapter.extra).notNullOrReport()
                } else {
                    listOf()
                }
            }

            override fun getImage(extra: String): URL {
                return novelManager.getImage(extra)
            }

            private fun URL.isHttp() = protocol.startsWith("http")

            override fun openImage(url: URL): InputStream? {
                return if (url.isHttp()) {
                    Glide.with(App.ctx)
                            .asFile()
                            .load(url.toString())
                            .apply(RequestOptions().onlyRetrieveFromCache(true))
                            .submit()
                            .get()
                            ?.inputStream()
                } else {
                    url.openStream()
                }

            }
        }
        exporter.export(info, contentProvider, progressCallback)
    }
}