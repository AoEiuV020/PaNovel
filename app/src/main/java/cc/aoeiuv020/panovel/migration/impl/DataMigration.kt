package cc.aoeiuv020.panovel.migration.impl

import android.content.Context
import cc.aoeiuv020.base.jar.get
import cc.aoeiuv020.base.jar.jsonPath
import cc.aoeiuv020.base.jar.toBean
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.data.entity.NovelMinimal
import cc.aoeiuv020.panovel.migration.Migration
import cc.aoeiuv020.panovel.settings.*
import cc.aoeiuv020.panovel.util.VersionName
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import java.io.File

/**
 * Created by AoEiuV020 on 2018.05.30-19:21:52.
 */
class DataMigration : Migration(), AnkoLogger {
    override val to: VersionName = VersionName("2.2.2")
    override val message: String = "书架列表，书单列表，设置，"

    override fun migrate(ctx: Context, from: VersionName) {
        import(ctx.getExternalFilesDir(null))
        import(ctx.filesDir)
    }

    private fun import(base: File) {
        importBookshelf(base)
        importBookList(base)
        importSettings(base)
    }

    private fun importMargins(base: File, margins: Margins) {
        val map = mapOf<String, (JsonElement) -> Unit>(
                "enabled" to { value -> margins.enabled = value.asBoolean },
                "left" to { value -> margins.left = value.asInt },
                "top" to { value -> margins.top = value.asInt },
                "right" to { value -> margins.right = value.asInt },
                "bottom" to { value -> margins.bottom = value.asInt }
        )
        base.listFiles()?.forEach { file ->
            val setter = map[file.name] ?: return@forEach
            file.readText().toBean<JsonElement>().let(setter)
        }
    }


    private fun importSettings(base: File) {
        val list = base.resolve("Settings").listFiles() ?: return
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
                "shareExpiration" to { value -> OtherSettings.shareExpiration = value.asString.toBean() }
        )
        val marginsMap = mapOf(
                "contentMargins" to ReaderSettings.contentMargins,
                "paginationMargins" to ReaderSettings.paginationMargins,
                "bookNameMargins" to ReaderSettings.bookNameMargins,
                "chapterNameMargins" to ReaderSettings.chapterNameMargins,
                "timeMargins" to ReaderSettings.timeMargins,
                "batteryMargins" to ReaderSettings.batteryMargins
        )
        list.forEach { file ->
            val setter = map[file.name] ?: return@forEach
            if (file.isFile) {
                file.readText().toBean<JsonElement>().let(setter)
            } else if (file.isDirectory) {
                val margins = marginsMap[file.name] ?: return@forEach
                importMargins(file, margins)
            }
        }
    }

    private fun importBookshelf(base: File) {
        val progress = base.resolve("Progress")
        val list = base.resolve("Bookshelf").listFiles()?.map {
            val novel = it.inputStream().jsonPath.run {
                Novel(site = get("$.site"),
                        author = get("$.author"),
                        name = get("$.name"),
                        detail = get("$.requester.extra"))
            }
            try {
                progress.resolve(novel.run { "$name.$author.$site" })
                        .takeIf { it.exists() }
                        ?.inputStream()?.jsonPath?.run {
                    novel.readAtChapterIndex = get("chapter")
                    novel.readAtTextIndex = get("text")
                }
            } catch (e: Exception) {
                error("旧版书架中的小说<${novel.run { "$name.$author.$site" }}>阅读进度读取失败,", e)
                // 进度次要，异常不抛出去，
            }
            novel
        } ?: return
        DataManager.importBookshelfWithProgress(list)
    }

    private fun importBookList(base: File) {
        base.resolve("BookList").listFiles()?.forEach {
            it.inputStream().jsonPath.run {
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
        }
    }
}