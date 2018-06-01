package cc.aoeiuv020.panovel.export

import android.content.Context
import android.os.Handler
import android.os.Looper
import cc.aoeiuv020.base.jar.ioExecutorService
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.util.notNull
import cc.aoeiuv020.panovel.util.notify
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.error
import java.io.File

/**
 * Created by AoEiuV020 on 2018.05.28-18:53:01.
 */
class TextExporter(
        private val ctx: Context
) : AnkoLogger {
    companion object {
        const val NAME_FOLDER = "Text"

        fun export(ctx: Context, novel: Novel) {
            TextExporter(ctx).exportExistsChapterToTextFile(novel)
        }
    }

    fun exportExistsChapterToTextFile(novel: Novel) {
        val exportingRunnable = object : Runnable {
            private val handler = Handler(Looper.getMainLooper())
            var export = 0
            var skip = 0
            var left = 0
            fun set(export: Int, skip: Int, left: Int) {
                this.export = export
                this.skip = skip
                this.left = left
            }

            fun start() {
                // 以防万一，先删除可能存在的自己，其实不会存在，
                handler.removeCallbacks(this)
                handler.post(this)
            }

            fun error() {
                // 导出失败结束通知循环，
                handler.removeCallbacks(this)
                ctx.notify(10000 + novel.nId.toInt(), text = ctx.getString(R.string.export_error_placeholder, export, skip, left), title = novel.name)
            }

            lateinit var file: File

            override fun run() {
                debug { "exporting <$export, $skip, $left>" }
                if (left == 0) {
                    // 导出完成结束通知循环，
                    handler.removeCallbacks(this)
                    ctx.notify(10000 + novel.nId.toInt(), text = ctx.getString(R.string.export_complete_placeholder, export, skip),
                            bigText = ctx.getString(R.string.export_complete_big_placeholder, file.path),
                            title = novel.name)
                } else {
                    ctx.notify(10000 + novel.nId.toInt(), text = ctx.getString(R.string.exporting_placeholder, export, skip, left), title = novel.name)
                    // 100ms循环一次通知，
                    handler.postDelayed(this, 100)
                }
            }
        }
        ctx.doAsync({ e ->
            val message = "导出小说失败，"
            Reporter.post(message, e)
            error(message, e)
            exportingRunnable.error()
        }, ioExecutorService) {
            // 尝试导出到sd卡，没有就导出到私有目录，虽然这样的导出好像没什么意义，
            val baseFile = ctx.getExternalFilesDir(null)
                    ?.resolve(NAME_FOLDER)
                    ?.apply { exists() || mkdirs() }
                    ?.takeIf { it.canWrite() }
                    ?: ctx.filesDir
                            .resolve(NAME_FOLDER)
                            .apply { exists() || mkdirs() }
            val fileName = novel.run { "$name.$author.$site.txt" }
            val file = baseFile.resolve(fileName)
            // 文件File存起来，用于导出完成时展示结果，
            exportingRunnable.file = file
            file.outputStream().bufferedWriter().use { output ->
                val chapters = DataManager.requestChapters(novel)
                val size = chapters.size
                var export = 0
                var skip = 0
                var left = size
                // 开始导出，
                exportingRunnable.set(export, skip, left)
                exportingRunnable.start()

                val container = DataManager.novelContentsCached(novel)
                chapters.forEach { chapter ->
                    // 章节之间空一行，
                    // 第一章前也空了一行，无所谓了，
                    output.appendln()
                    output.write(chapter.name)
                    output.newLine()
                    left--
                    if (container.contains(chapter.extra)) {
                        export++
                        // 判断过章节存在了，这个必须非空，除非导出过程删除了缓存，
                        val content = DataManager.getContent(novel, chapter).notNull()
                        // 逐行写入，
                        content.forEach {
                            output.appendln("　　$it")
                        }
                    } else {
                        skip++
                    }
                    exportingRunnable.set(export, skip, left)
                }

                // 以防万一，关闭前刷个缓冲，
                output.flush()
            }
        }
    }
}