package cc.aoeiuv020.panovel.export.impl

import cc.aoeiuv020.base.jar.toJson
import cc.aoeiuv020.panovel.App
import cc.aoeiuv020.panovel.export.ExportOption
import cc.aoeiuv020.panovel.export.ExportOption.*
import cc.aoeiuv020.panovel.settings.GeneralSettings
import cc.aoeiuv020.panovel.settings.ListSettings
import cc.aoeiuv020.panovel.settings.OtherSettings
import cc.aoeiuv020.panovel.settings.ReaderSettings
import cc.aoeiuv020.panovel.util.Pref
import org.jetbrains.anko.debug
import java.io.File

/**
 * Created by AoEiuV020 on 2018.05.30-20:40:56.
 */
class ExporterV2 : DefaultExporter() {
    override fun import(file: File, option: ExportOption): Int {
        debug { "import $option from $file" }
        return 0
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun export(file: File, option: ExportOption): Int {
        debug { "export $option to $file" }
        return when (option) {
            Bookshelf -> TODO()
            BookList -> TODO()
            Settings -> exportSettings(file)
        }
    }

    // 设置分多个文件导出，
    private fun exportSettings(folder: File): Int {
        folder.mkdirs()
        @Suppress("RemoveExplicitTypeArguments")
        var count = listOf<Pref>(
                GeneralSettings, ListSettings, OtherSettings, ReaderSettings,
                ReaderSettings.batteryMargins,
                ReaderSettings.bookNameMargins,
                ReaderSettings.chapterNameMargins,
                ReaderSettings.contentMargins,
                ReaderSettings.paginationMargins,
                ReaderSettings.timeMargins
        ).sumBy { pref ->
            pref.sharedPreferences.all.also {
                folder.resolve(pref.name).writeText(it.toJson())
            }.size
        }
        val backgroundImage = ReaderSettings.backgroundImage
        if (backgroundImage != null) {
            folder.resolve("backgroundImage").outputStream().use { output ->
                App.ctx.contentResolver.openInputStream(backgroundImage).use { input ->
                    input.copyTo(output)
                }
                output.flush()
            }
            count++
        }
        val font = ReaderSettings.font
        if (font != null) {
            folder.resolve("font").outputStream().use { output ->
                App.ctx.contentResolver.openInputStream(font).use { input ->
                    input.copyTo(output)
                }
                output.flush()
            }
            count++
        }
        return count
    }
}