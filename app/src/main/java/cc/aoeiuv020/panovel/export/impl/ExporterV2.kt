package cc.aoeiuv020.panovel.export.impl

import android.net.Uri
import cc.aoeiuv020.base.jar.divide
import cc.aoeiuv020.base.jar.toBean
import cc.aoeiuv020.base.jar.toJson
import cc.aoeiuv020.panovel.App
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.data.entity.NovelMinimal
import cc.aoeiuv020.panovel.data.entity.NovelWithProgress
import cc.aoeiuv020.panovel.export.ExportOption
import cc.aoeiuv020.panovel.export.ExportOption.*
import cc.aoeiuv020.panovel.settings.GeneralSettings
import cc.aoeiuv020.panovel.settings.ListSettings
import cc.aoeiuv020.panovel.settings.OtherSettings
import cc.aoeiuv020.panovel.settings.ReaderSettings
import cc.aoeiuv020.panovel.util.Pref
import com.google.gson.JsonElement
import org.jetbrains.anko.debug
import java.io.File

/**
 * Created by AoEiuV020 on 2018.05.30-20:40:56.
 */
class ExporterV2 : DefaultExporter() {
    override fun import(file: File, option: ExportOption): Int {
        debug { "import $option from $file" }
        return when (option) {
            Bookshelf -> importBookshelf(file)
            BookList -> importBookList(file)
            Settings -> importSettings(file)
        }
    }

    private fun importSettings(folder: File): Int {
        val list = folder.listFiles()
        return list.sumBy { file ->
            when (file.name) {
                "General" -> importPref(GeneralSettings, file)
                "List" -> importPref(ListSettings, file)
                "Other" -> importPref(OtherSettings, file)
                "Reader" -> importPref(ReaderSettings, file)
                "Reader_BatteryMargins" -> importPref(ReaderSettings.batteryMargins, file)
                "Reader_BookNameMargins" -> importPref(ReaderSettings.bookNameMargins, file)
                "Reader_ChapterNameMargins" -> importPref(ReaderSettings.chapterNameMargins, file)
                "Reader_ContentMargins" -> importPref(ReaderSettings.contentMargins, file)
                "Reader_PaginationMargins" -> importPref(ReaderSettings.paginationMargins, file)
                "Reader_TimeMargins" -> importPref(ReaderSettings.timeMargins, file)
                "backgroundImage" -> 1.also { ReaderSettings.backgroundImage = Uri.fromFile(file) }
                "lastBackgroundImage" -> 1.also { ReaderSettings.lastBackgroundImage = Uri.fromFile(file) }
                "font" -> 1.also { ReaderSettings.font = Uri.fromFile(file) }
                else -> 0
            }
        }
    }

    private fun importPref(pref: Pref, file: File): Int {
        val editor = pref.sharedPreferences.edit()
        var count = 0
        file.readText().toBean<Map<String, JsonElement>>().forEach { (key, value) ->
            when (key) {
                "animationMode" -> editor.putString(key, value.asString)
                "shareExpiration" -> editor.putString(key, value.asString)
                "onCheckUpdateClick" -> editor.putString(key, value.asString)
                "onDotClick" -> editor.putString(key, value.asString)
                "onDotLongClick" -> editor.putString(key, value.asString)
                "onItemClick" -> editor.putString(key, value.asString)
                "onItemLongClick" -> editor.putString(key, value.asString)
                "onLastChapterClick" -> editor.putString(key, value.asString)
                "onNameClick" -> editor.putString(key, value.asString)
                "onNameLongClick" -> editor.putString(key, value.asString)
                "adEnabled" -> editor.putBoolean(key, value.asBoolean)
                "keepScreenOn" -> editor.putBoolean(key, value.asBoolean)
                "backPressOutOfFullScreen" -> editor.putBoolean(key, value.asBoolean)
                "fullScreenClickNextPage" -> editor.putBoolean(key, value.asBoolean)
                "gridView" -> editor.putBoolean(key, value.asBoolean)
                "largeView" -> editor.putBoolean(key, value.asBoolean)
                "reportCrash" -> editor.putBoolean(key, value.asBoolean)
                "volumeKeyScroll" -> editor.putBoolean(key, value.asBoolean)
                "animationSpeed" -> editor.putFloat(key, value.asFloat)
                "centerPercent" -> editor.putFloat(key, value.asFloat)
                "dotSize" -> editor.putFloat(key, value.asFloat)
                "autoSaveReadStatus" -> editor.putInt(key, value.asInt)
                "brightness" -> editor.putInt(key, value.asInt)
                "autoRefreshInterval" -> editor.putInt(key, value.asInt)
                "backgroundColor" -> editor.putInt(key, value.asInt)
                "lastBackgroundColor" -> editor.putInt(key, value.asInt)
                "chapterColorCached" -> editor.putInt(key, value.asInt)
                "chapterColorDefault" -> editor.putInt(key, value.asInt)
                "chapterColorReadAt" -> editor.putInt(key, value.asInt)
                "dotColor" -> editor.putInt(key, value.asInt)
                "downloadThreadsLimit" -> editor.putInt(key, value.asInt)
                "searchThreadsLimit" -> editor.putInt(key, value.asInt)
                "downloadCount" -> editor.putInt(key, value.asInt)
                "fullScreenDelay" -> editor.putInt(key, value.asInt)
                "historyCount" -> editor.putInt(key, value.asInt)
                "lineSpacing" -> editor.putInt(key, value.asInt)
                "messageSize" -> editor.putInt(key, value.asInt)
                "paragraphSpacing" -> editor.putInt(key, value.asInt)
                "textColor" -> editor.putInt(key, value.asInt)
                "lastTextColor" -> editor.putInt(key, value.asInt)
                "textSize" -> editor.putInt(key, value.asInt)
                "dateFormat" -> editor.putString(key, value.asString)
                "enabled" -> editor.putBoolean(key, value.asBoolean)
                "notifyNovelUpdate" -> editor.putBoolean(key, value.asBoolean)
                "askUpdate" -> editor.putBoolean(key, value.asBoolean)
                "singleNotification" -> editor.putBoolean(key, value.asBoolean)
                "notifyPinnedOnly" -> editor.putBoolean(key, value.asBoolean)
                "dotNotifyUpdate" -> editor.putBoolean(key, value.asBoolean)
                "bottom" -> editor.putInt(key, value.asInt)
                "left" -> editor.putInt(key, value.asInt)
                "right" -> editor.putInt(key, value.asInt)
                "top" -> editor.putInt(key, value.asInt)
                else -> --count
            }
            ++count
        }
        editor.apply()
        return count
    }

    private fun importBookList(folder: File): Int = folder.listFiles().sumBy { file ->
        val name = file.name.divide('|').second
        val novelList = file.readText().toBean<List<NovelMinimal>>()
        DataManager.importBookList(name, novelList)
        novelList.size
    }

    private fun importBookshelf(file: File): Int {
        val list = file.readText().toBean<List<NovelWithProgress>>()
        DataManager.importBookshelfWithProgress(list)
        return list.size
    }

    override fun export(file: File, option: ExportOption): Int {
        debug { "export $option to $file" }
        return when (option) {
            Bookshelf -> exportBookshelf(file)
            BookList -> exportBookList(file)
            Settings -> exportSettings(file)
        }
    }

    // 书架只导出一个文件，
    private fun exportBookshelf(file: File): Int {
        val list = DataManager.listBookshelf().map {
            NovelWithProgress(it.novel)
        }
        file.writeText(list.toJson())
        return list.size
    }

    // 书单分多个文件导出，一个书单一个文件，
    private fun exportBookList(folder: File): Int {
        folder.mkdirs()
        return DataManager.allBookList().sumBy { bookList ->
            // 书单名允许重复，所以拼接上id，
            val fileName = "${bookList.id}|${bookList.name}"
            // 只取小说必须的几个参数，相关数据类不能被混淆，
            // 不包括本地小说，
            val novelList = DataManager.getNovelMinimalFromBookList(bookList.nId)
            folder.resolve(fileName).writeText(novelList.toJson())
            novelList.size
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
            // 直接从sp读map, 不受几个Settings混淆影响，
            pref.sharedPreferences.all.also {
                folder.resolve(pref.name).writeText(it.toJson())
            }.size
        }
        // 导出背景图片，
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
        // 导出前一次设置的背景图片，
        val lastBackgroundImage = ReaderSettings.lastBackgroundImage
        if (lastBackgroundImage != null) {
            folder.resolve("lastBackgroundImage").outputStream().use { output ->
                App.ctx.contentResolver.openInputStream(lastBackgroundImage).use { input ->
                    input.copyTo(output)
                }
                output.flush()
            }
            count++
        }
        // 导出字体，
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