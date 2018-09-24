package cc.aoeiuv020.panovel.backup.impl

import cc.aoeiuv020.base.jar.get
import cc.aoeiuv020.base.jar.jsonPath
import cc.aoeiuv020.gson.toBean
import cc.aoeiuv020.panovel.backup.BackupOption
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.data.entity.NovelMinimal
import cc.aoeiuv020.panovel.data.entity.NovelWithProgress
import cc.aoeiuv020.panovel.settings.*
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.error
import java.io.File

/**
 * Created by AoEiuV020 on 2018.05.11-18:52:50.
 */
class BackupV1 : DefaultBackup(), AnkoLogger {
    override fun import(file: File, option: BackupOption): Int {
        debug { "import $option" }

        return when (option) {
            BackupOption.Bookshelf -> {
                val list = file.readText().jsonPath.get<JsonArray>().map {
                    it.jsonPath.run {
                        NovelWithProgress(site = get("$.item.site"),
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
            BackupOption.BookList -> {
                file.readText().jsonPath.get<JsonArray>().onEach {
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
            BackupOption.Settings -> {
                val map = mapOf<String, (JsonElement) -> Unit>(
                        "backPressOutOfFullScreen" to { value -> ReaderSettings.backPressOutOfFullScreen = value.asBoolean },
                        "adEnabled" to { value -> GeneralSettings.adEnabled = value.asBoolean },
                        "BookSmallLayout" to { value -> ListSettings.largeView = !value.asBoolean },
                        "fullScreenClickNextPage" to { value -> ReaderSettings.fullScreenClickNextPage = value.asBoolean },
                        "volumeKeyScroll" to { value -> ReaderSettings.volumeKeyScroll = value.asBoolean },
                        "reportCrash" to { value -> OtherSettings.reportCrash = value.asBoolean },
                        "subscribeNovelUpdate" to { value -> ServerSettings.notifyNovelUpdate = value.asBoolean },
                        "bookshelfRedDotNotifyNotReadOrNewChapter" to { value -> ListSettings.dotNotifyUpdate = value.asBoolean },
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
                file.readText().jsonPath.get<Map<String, JsonElement>>().entries.forEach { (key, value) ->
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

    fun Margins.import(json: String) {
        val map = mapOf<String, (JsonElement) -> Unit>(
                "enabled" to { value -> enabled = value.asBoolean },
                "left" to { value -> left = value.asInt },
                "top" to { value -> top = value.asInt },
                "right" to { value -> right = value.asInt },
                "bottom" to { value -> bottom = value.asInt }
        )
        json.toBean<JsonObject>().entrySet().forEach { (key, value) ->
            map[key]?.invoke(value)
        }
    }
}
