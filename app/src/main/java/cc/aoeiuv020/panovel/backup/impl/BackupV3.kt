package cc.aoeiuv020.panovel.backup.impl

import android.net.Uri
import cc.aoeiuv020.gson.toBean
import cc.aoeiuv020.panovel.backup.BackupOption
import cc.aoeiuv020.panovel.backup.BackupOption.*
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.data.entity.NovelMinimal
import cc.aoeiuv020.panovel.data.entity.NovelWithProgressAndPinnedTime
import cc.aoeiuv020.panovel.settings.*
import cc.aoeiuv020.panovel.share.Share
import cc.aoeiuv020.panovel.util.Pref
import cc.aoeiuv020.panovel.util.notNullOrReport
import com.google.gson.JsonElement
import org.jetbrains.anko.debug
import java.io.File
import java.util.*

/**
 * Created by AoEiuV020 on 2018.05.30-20:40:56.
 */
class BackupV3 : DefaultBackup() {
    override fun import(file: File, option: BackupOption): Int {
        debug { "import $option from $file" }
        return when (option) {
            Bookshelf -> importBookshelf(file)
            BookList -> importBookList(file)
            Progress -> importProgress(file)
            Settings -> importSettings(file)
        }
    }

    private fun importProgress(file: File): Int {
        return file.useLines { s ->
            s.map { line ->
                val a = line.split(',')
                NovelWithProgressAndPinnedTime(
                        a[0],
                        a[1],
                        a[2],
                        a[3],
                        a[4].toInt(),
                        a[5].toInt(),
                        Date(a[6].toLong())
                )
            }.let {
                DataManager.importNovelWithProgress(it)
            }
        }
    }

    private fun importSettings(folder: File): Int {
        val list = folder.listFiles()
        return list.notNullOrReport().sumBy { file ->
            when (file.name) {
                "Ad" -> importPref(AdSettings, file)
                "General" -> importPref(GeneralSettings, file)
                "List" -> importPref(ListSettings, file)
                "Other" -> importPref(OtherSettings, file)
                "Reader" -> importPref(ReaderSettings, file)
                "Download" -> importPref(DownloadSettings, file)
                "Interface" -> importPref(InterfaceSettings, file)
                "Location" -> importPref(LocationSettings, file)
                "Server" -> importPref(ServerSettings, file)
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
                // 枚举，保存字符串，
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
                "bookshelfOrderBy" -> editor.putString(key, value.asString)

                // 兼容广告开关从General移到Ad,
                "adEnabled" -> AdSettings.adEnabled = value.asBoolean
                "keepScreenOn" -> editor.putBoolean(key, value.asBoolean)
                "fullScreen" -> editor.putBoolean(key, value.asBoolean)
                "backPressOutOfFullScreen" -> editor.putBoolean(key, value.asBoolean)
                "fullScreenClickNextPage" -> editor.putBoolean(key, value.asBoolean)
                "fitWidth" -> editor.putBoolean(key, value.asBoolean)
                "fitHeight" -> editor.putBoolean(key, value.asBoolean)
                "gridView" -> editor.putBoolean(key, value.asBoolean)
                "largeView" -> editor.putBoolean(key, value.asBoolean)
                "pinnedBackgroundColor" -> editor.putInt(key, value.asInt)
                "refreshOnSearch" -> editor.putBoolean(key, value.asBoolean)
                "reportCrash" -> editor.putBoolean(key, value.asBoolean)
                "volumeKeyScroll" -> editor.putBoolean(key, value.asBoolean)
                "tabGravityCenter" -> editor.putBoolean(key, value.asBoolean)
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
                "searchThreadsLimit" -> editor.putInt(key, value.asInt)
                // 下载相关设置以前是在GeneralSettings里，
                "downloadThreadsLimit" -> DownloadSettings.downloadThreadsLimit = value.asInt
                "downloadCount" -> DownloadSettings.downloadCount = value.asInt
                "autoDownloadCount" -> DownloadSettings.autoDownloadCount = value.asInt
                "fullScreenDelay" -> editor.putInt(key, value.asInt)
                "historyCount" -> editor.putInt(key, value.asInt)
                "lineSpacing" -> editor.putInt(key, value.asInt)
                "messageSize" -> editor.putInt(key, value.asInt)
                "paragraphSpacing" -> editor.putInt(key, value.asInt)
                "textColor" -> editor.putInt(key, value.asInt)
                "lastTextColor" -> editor.putInt(key, value.asInt)
                "textSize" -> editor.putInt(key, value.asInt)
                "dateFormat" -> editor.putString(key, value.asString)
                "segmentIndentation" -> editor.putString(key, value.asString)
                "enabled" -> editor.putBoolean(key, value.asBoolean)
                "serverAddress" -> editor.putString(key, value.asString)
                "notifyNovelUpdate" -> editor.putBoolean(key, value.asBoolean)
                "askUpdate" -> editor.putBoolean(key, value.asBoolean)
                "singleNotification" -> editor.putBoolean(key, value.asBoolean)
                "notifyPinnedOnly" -> editor.putBoolean(key, value.asBoolean)
                "dotNotifyUpdate" -> editor.putBoolean(key, value.asBoolean)
                "subscriptToast" -> editor.putBoolean(key, value.asBoolean)
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

    private fun importBookList(folder: File): Int = folder.listFiles().notNullOrReport().sumBy { file ->
        val bookListBean = Share.importBookList(file.readText())
        DataManager.importBookList(
                bookListBean.name,
                bookListBean.list,
                bookListBean.uuid
        )
        bookListBean.list.size
    }

    private fun importBookshelf(file: File): Int {
        val list = file.readText().toBean<List<NovelMinimal>>()
        DataManager.importBookshelf(list)
        return list.size
    }

}