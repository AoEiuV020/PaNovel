package cc.aoeiuv020.panovel.export

import android.content.Context
import cc.aoeiuv020.base.jar.toJson
import cc.aoeiuv020.base.jar.type
import cc.aoeiuv020.panovel.local.*
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.error
import java.io.InputStream
import java.io.OutputStream

/**
 * Created by AoEiuV020 on 2018.05.11-18:52:50.
 */
class ExporterV1(ctx: Context) : DefaultExporter(ctx), AnkoLogger {
    /**
     * TODO: 这个gson忘了处理requester特殊的序列化，但目前只有DetailRequester, 就这样也行，
     */
    private val gson = GsonBuilder()
            .create()

    private fun Any.toJson(): String = toJson(gson)
    private inline fun <reified T> T.toJson(output: OutputStream): T = this.apply {
        output.writer().also {
            gson.toJson(this, it)
            it.flush()
        }
    }

    // reified T 可以直接给gson用，没有reified的T用TypeToken包装也没用，只能传入type,
    private inline fun <reified T> InputStream.toBean(): T = gson.fromJson(this.reader(), type<T>())

    override fun import(input: InputStream, option: ExportOption): Int {
        debug {
            "import $option"
        }
        return when (option) {
            ExportOption.Bookshelf -> {
                input.toBean<List<NovelItemWithProgress>>().also {
                    it.forEach {
                        Bookshelf.add(it.item)
                        Progress.save(it.item, it.progress)
                    }
                }.size
            }
            ExportOption.BookList -> {
                input.toBean<List<BookListData>>().also {
                    it.forEach {
                        BookList.put(it)
                    }
                }.size
            }
            ExportOption.Settings -> {
                input.toBean<JsonObject>().also {
                    it.entrySet().forEach { (key, value) ->
                        try {
                            when (key) {
                                "bookshelfRedDotNotifyNotReadOrNewChapter" -> Settings.bookshelfRedDotNotifyNotReadOrNewChapter = value.asBoolean
                                "bookshelfShowMoreActionDot" -> Settings.bookshelfShowMoreActionDot = value.asBoolean
                                "bookshelfAutoRefresh" -> Settings.bookshelfAutoRefresh = value.asBoolean
                                "backPressOutOfFullScreen" -> Settings.backPressOutOfFullScreen = value.asBoolean
                                "adEnabled" -> Settings.adEnabled = value.asBoolean
                                "BookSmallLayout" -> Settings.BookSmallLayout = value.asBoolean
                                "bookListAutoSave" -> Settings.bookListAutoSave = value.asBoolean
                                "fullScreenClickNextPage" -> Settings.fullScreenClickNextPage = value.asBoolean
                                "volumeKeyScroll" -> Settings.volumeKeyScroll = value.asBoolean
                                "reportCrash" -> Settings.reportCrash = value.asBoolean
                                "subscribeNovelUpdate" -> Settings.subscribeNovelUpdate = value.asBoolean
                                "bookshelfRedDotColor" -> Settings.bookshelfRedDotColor = value.asInt
                                "fullScreenDelay" -> Settings.fullScreenDelay = value.asInt
                                "textSize" -> Settings.textSize = value.asInt
                                "lineSpacing" -> Settings.lineSpacing = value.asInt
                                "paragraphSpacing" -> Settings.paragraphSpacing = value.asInt
                                "messageSize" -> Settings.messageSize = value.asInt
                                "autoRefreshInterval" -> Settings.autoRefreshInterval = value.asInt
                                "textColor" -> Settings.textColor = value.asInt
                                "backgroundColor" -> Settings.backgroundColor = value.asInt
                                "historyCount" -> Settings.historyCount = value.asInt
                                "asyncThreadCount" -> Settings.asyncThreadCount = value.asInt
                                "downloadThreadCount" -> Settings.downloadThreadCount = value.asInt
                                "chapterColorDefault" -> Settings.chapterColorDefault = value.asInt
                                "chapterColorCached" -> Settings.chapterColorCached = value.asInt
                                "chapterColorReadAt" -> Settings.chapterColorReadAt = value.asInt
                                "bookshelfRedDotSize" -> Settings.bookshelfRedDotSize = value.asFloat
                                "animationSpeed" -> Settings.animationSpeed = value.asFloat
                                "centerPercent" -> Settings.centerPercent = value.asFloat
                                "dateFormat" -> Settings.dateFormat = value.asString
                                "animationMode" -> Settings.animationMode = value.asString.toBean()
                                "shareExpiration" -> Settings.shareExpiration = value.asString.toBean()
                                "contentMargins" -> Settings.contentMargins.import(gson, value.asString)
                                "paginationMargins" -> Settings.paginationMargins.import(gson, value.asString)
                                "bookNameMargins" -> Settings.bookNameMargins.import(gson, value.asString)
                                "chapterNameMargins" -> Settings.chapterNameMargins.import(gson, value.asString)
                                "timeMargins" -> Settings.timeMargins.import(gson, value.asString)
                                "batteryMargins" -> Settings.batteryMargins.import(gson, value.asString)
                            }
                        } catch (e: Exception) {
                            // 只是一个设置读取失败的话可以继续，
                            error("设置<$key>读取失败，", e)
                        }
                    }
                }.size()
            }
        }
    }

    override fun export(output: OutputStream, option: ExportOption): Int {
        debug {
            "export $option"
        }
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
    }
}
