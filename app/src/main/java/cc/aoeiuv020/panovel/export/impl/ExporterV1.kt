package cc.aoeiuv020.panovel.export.impl

import android.content.Context
import cc.aoeiuv020.base.jar.get
import cc.aoeiuv020.base.jar.jsonPath
import cc.aoeiuv020.base.jar.toBean
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.data.entity.NovelMinimal
import cc.aoeiuv020.panovel.export.ExportOption
import cc.aoeiuv020.panovel.settings.GeneralSettings
import cc.aoeiuv020.panovel.settings.ListSettings
import cc.aoeiuv020.panovel.settings.OtherSettings
import cc.aoeiuv020.panovel.settings.ReaderSettings
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.error
import java.io.InputStream
import java.io.OutputStream

/**
 * Created by AoEiuV020 on 2018.05.11-18:52:50.
 */
class ExporterV1(ctx: Context) : SingleFileExporter(ctx), AnkoLogger {
    override fun import(input: InputStream, option: ExportOption): Int {
        debug {
            "import $option"
        }

        return when (option) {
            ExportOption.Bookshelf -> {
                val list = input.jsonPath.get<JsonArray>().map {
                    it.jsonPath.run {
                        Novel(site = get("$.item.site"),
                                author = get("$.item.author"),
                                name = get("$.item.name"),
                                detail = get("$.item.requester.extra"),
                                readAtChapterIndex = get("$.progress.chapter"),
                                readAtTextIndex = get("$.progress.text"))
                    }
                }
                DataManager.importBookshelfWithProgress(list)
                list.size
            }
            ExportOption.BookList -> {
                input.jsonPath.get<JsonArray>().onEach {
                    it.jsonPath.run {
                        val name = get<String>("$.name")
                        val list = get<JsonArray>("$.list").map {
                            it.jsonPath.run {
                                NovelMinimal(site = get("$.site"),
                                        author = get("$.author"),
                                        name = get("$.name"),
                                        detail = get("$.requester.extra"))
                            }
                        }
                        DataManager.importBookList(name, list)
                    }
                }.size()
            }
            ExportOption.Settings -> {
                val map = mapOf<String, (JsonElement) -> Unit>(
                        "backPressOutOfFullScreen" to { value -> ReaderSettings.backPressOutOfFullScreen = value.asBoolean },
                        "adEnabled" to { value -> GeneralSettings.adEnabled = value.asBoolean },
                        "BookSmallLayout" to { value -> ListSettings.largeView = !value.asBoolean },
                        "fullScreenClickNextPage" to { value -> ReaderSettings.fullScreenClickNextPage = value.asBoolean },
                        "volumeKeyScroll" to { value -> ReaderSettings.volumeKeyScroll = value.asBoolean },
                        "reportCrash" to { value -> OtherSettings.reportCrash = value.asBoolean },
                        "bookshelfRedDotColor" to { value -> ListSettings.dotColor = value.asInt },
                        "bookshelfRedDotSize" to { value -> ListSettings.dotSize = value.asFloat },
                        "fullScreenDelay" to { value -> ReaderSettings.fullScreenDelay = value.asInt },
                        "textSize" to { value -> ReaderSettings.textSize = value.asInt },
                        "lineSpacing" to { value -> ReaderSettings.lineSpacing = value.asInt },
                        "paragraphSpacing" to { value -> ReaderSettings.paragraphSpacing = value.asInt },
                        "messageSize" to { value -> ReaderSettings.messageSize = value.asInt },
                        "autoRefreshInterval" to { value -> ReaderSettings.autoRefreshInterval = value.asInt },
                        "textColor" to { value -> ReaderSettings.textColor = value.asInt },
                        "backgroundColor" to { value -> ReaderSettings.backgroundColor = value.asInt },
                        "historyCount" to { value -> GeneralSettings.historyCount = value.asInt },
                        "downloadThreadCount" to { value -> GeneralSettings.downloadThreadsLimit = value.asInt },
                        "chapterColorDefault" to { value -> OtherSettings.chapterColorDefault = value.asInt },
                        "chapterColorCached" to { value -> OtherSettings.chapterColorCached = value.asInt },
                        "chapterColorReadAt" to { value -> OtherSettings.chapterColorReadAt = value.asInt },
                        "animationSpeed" to { value -> ReaderSettings.animationSpeed = value.asFloat },
                        "centerPercent" to { value -> ReaderSettings.centerPercent = value.asFloat },
                        "dateFormat" to { value -> ReaderSettings.dateFormat = value.asString },
                        "animationMode" to { value -> ReaderSettings.animationMode = value.asString.toBean() },
                        "shareExpiration" to { value -> OtherSettings.shareExpiration = value.asString.toBean() },
                        "contentMargins" to { value -> ReaderSettings.contentMargins.import(value.asString) },
                        "paginationMargins" to { value -> ReaderSettings.paginationMargins.import(value.asString) },
                        "bookNameMargins" to { value -> ReaderSettings.bookNameMargins.import(value.asString) },
                        "chapterNameMargins" to { value -> ReaderSettings.chapterNameMargins.import(value.asString) },
                        "timeMargins" to { value -> ReaderSettings.timeMargins.import(value.asString) },
                        "batteryMargins" to { value -> ReaderSettings.batteryMargins.import(value.asString) }
                )
                var count = 0
                input.jsonPath.get<Map<String, JsonElement>>().entries.forEach { (key, value) ->
                    try {
                        map[key]?.let {
                            it(value)
                            ++count
                        }
                    } catch (e: Exception) {
                        // 只是一个设置读取失败的话可以继续，
                        error("设置<$key>读取失败，", e)
                    }
                }
                count
            }
        }
    }

    override fun export(output: OutputStream, option: ExportOption): Int {
        debug {
            "export $option"
        }
/*
        return when (option) {
            ExportOption.Bookshelf -> {
                Bookshelf.list().map {
                    NovelItemWithProgress(it, Progress.load(it))
                }.toJson(output)
                        .size
            }
            ExportOption.BookList -> {
                BookList.list().toJson(output)
                        .size
            }
        // 备份设置不包括字体和背景图片，
            ExportOption.Settings -> {
                JsonObject().apply {
                    addProperty("bookshelfRedDotNotifyNotReadOrNewChapter", Settings.bookshelfRedDotNotifyNotReadOrNewChapter)
                    addProperty("bookshelfRedDotSize", Settings.bookshelfRedDotSize)
                    addProperty("bookshelfRedDotColor", Settings.bookshelfRedDotColor)
                    addProperty("bookshelfShowMoreActionDot", Settings.bookshelfShowMoreActionDot)
                    addProperty("bookshelfAutoRefresh", Settings.bookshelfAutoRefresh)
                    addProperty("fullScreenDelay", Settings.fullScreenDelay)
                    addProperty("backPressOutOfFullScreen", Settings.backPressOutOfFullScreen)
                    addProperty("textSize", Settings.textSize)
                    addProperty("lineSpacing", Settings.lineSpacing)
                    addProperty("paragraphSpacing", Settings.paragraphSpacing)
                    addProperty("contentMargins", Settings.contentMargins.export(gson))
                    addProperty("paginationMargins", Settings.paginationMargins.export(gson))
                    addProperty("bookNameMargins", Settings.bookNameMargins.export(gson))
                    addProperty("chapterNameMargins", Settings.chapterNameMargins.export(gson))
                    addProperty("timeMargins", Settings.timeMargins.export(gson))
                    addProperty("batteryMargins", Settings.batteryMargins.export(gson))
                    addProperty("messageSize", Settings.messageSize)
                    addProperty("autoRefreshInterval", Settings.autoRefreshInterval)
                    addProperty("dateFormat", Settings.dateFormat)
                    addProperty("textColor", Settings.textColor)
                    addProperty("backgroundColor", Settings.backgroundColor)
                    addProperty("historyCount", Settings.historyCount)
                    addProperty("asyncThreadCount", Settings.asyncThreadCount)
                    addProperty("downloadThreadCount", Settings.downloadThreadCount)
                    addProperty("adEnabled", Settings.adEnabled)
                    addProperty("BookSmallLayout", Settings.BookSmallLayout)
                    addProperty("bookListAutoSave", Settings.bookListAutoSave)
                    addProperty("chapterColorDefault", Settings.chapterColorDefault)
                    addProperty("chapterColorCached", Settings.chapterColorCached)
                    addProperty("chapterColorReadAt", Settings.chapterColorReadAt)
                    addProperty("animationMode", Settings.animationMode.toJson())
                    addProperty("animationSpeed", Settings.animationSpeed)
                    addProperty("fullScreenClickNextPage", Settings.fullScreenClickNextPage)
                    addProperty("volumeKeyScroll", Settings.volumeKeyScroll)
                    addProperty("centerPercent", Settings.centerPercent)
                    addProperty("shareExpiration", Settings.shareExpiration.toJson())
                    addProperty("reportCrash", Settings.reportCrash)
                    addProperty("subscribeNovelUpdate", Settings.subscribeNovelUpdate)
                }.toJson(output)
                        .size()
            }
        }
*/
        TODO("导出，")
    }
}
