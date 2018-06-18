package cc.aoeiuv020.reader

import android.graphics.Typeface
import android.net.Uri
import cc.aoeiuv020.reader.ReaderConfigName.*
import kotlin.reflect.KProperty

/**
 *
 * Created by AoEiuV020 on 2017.12.01-22:24:49.
 */
class ReaderConfig(
        textSize: Int,
        lineSpacing: Int,
        paragraphSpacing: Int,
        contentMargins: ItemMargins,
        paginationMargins: ItemMargins,
        bookNameMargins: ItemMargins,
        chapterNameMargins: ItemMargins,
        timeMargins: ItemMargins,
        batteryMargins: ItemMargins,
        messageSize: Int,
        dateFormat: String,
        textColor: Int,
        backgroundColor: Int,
        backgroundImage: Uri?,
        animationMode: AnimationMode = AnimationMode.SIMPLE,
        animationSpeed: Float = 0.8f,
        font: Typeface? = null,
        centerPercent: Float = 0.5f,
        /**
         * 下面的不支持阅读中修改，省事，
         */
        var autoRefreshInterval: Int = 0,
        var fullScreenClickNextPage: Boolean = false
) {
    internal var listeners = mutableListOf<ConfigChangedListener>()

    var textSize: Int by ConfigDelegate(textSize, TextSize)
    var lineSpacing: Int by ConfigDelegate(lineSpacing, LineSpacing)
    var paragraphSpacing: Int  by ConfigDelegate(paragraphSpacing, ParagraphSpacing)

    var contentMargins: ItemMargins by ConfigDelegate(contentMargins, ContentMargins)

    var paginationMargins: ItemMargins by ConfigDelegate(paginationMargins, PaginationMargins)
    var timeMargins: ItemMargins by ConfigDelegate(timeMargins, TimeMargins)
    var batteryMargins: ItemMargins by ConfigDelegate(batteryMargins, BatteryMargins)
    var bookNameMargins: ItemMargins by ConfigDelegate(bookNameMargins, BookNameMargins)
    var chapterNameMargins: ItemMargins by ConfigDelegate(chapterNameMargins, ChapterNameMargins)
    var messageSize: Int by ConfigDelegate(messageSize, MessageSize)
    var dateFormat: String by ConfigDelegate(dateFormat, DateFormat)

    var textColor: Int by ConfigDelegate(textColor, TextColor)
    var backgroundColor: Int by ConfigDelegate(backgroundColor, BackgroundColor)
    var backgroundImage: Uri? by ConfigDelegate(backgroundImage, BackgroundImage)
    var animationMode: AnimationMode by ConfigDelegate(animationMode, ReaderConfigName.AnimationMode)
    var animationSpeed: Float by ConfigDelegate(animationSpeed, AnimDurationMultiply)
    var font: Typeface? = font
        set(value) {
            field = value
            titleFont = Typeface.create(value, Typeface.BOLD)
            listeners.forEach {
                it.onConfigChanged(ReaderConfigName.Font)
            }
        }
    /**
     * 修改字体时也要修改这个标题字体，
     */
    var titleFont: Typeface? = Typeface.create(font, Typeface.BOLD)
    var centerPercent: Float = centerPercent
        set(value) {
            field = value
            listeners.forEach {
                it.onConfigChanged(ReaderConfigName.CenterPercent)
            }
        }

    class ConfigDelegate<T>(default: T, val name: ReaderConfigName) {
        private var backingField: T = default
        operator fun getValue(thisRef: ReaderConfig, property: KProperty<*>): T {
            return backingField
        }

        operator fun setValue(thisRef: ReaderConfig, property: KProperty<*>, value: T) {
            backingField = value
            thisRef.listeners.forEach {
                it.onConfigChanged(name)
            }
        }
    }

}
