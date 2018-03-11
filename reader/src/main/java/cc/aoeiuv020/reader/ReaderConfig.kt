package cc.aoeiuv020.reader

import android.graphics.Typeface
import android.net.Uri
import cc.aoeiuv020.pager.IMargins
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
        contentMargins: IMargins,
        paginationMargins: IMargins,
        bookNameMargins: IMargins,
        chapterNameMargins: IMargins,
        timeMargins: IMargins,
        batteryMargins: IMargins,
        paginationEnabled: Boolean,
        bookNameEnabled: Boolean,
        chapterNameEnabled: Boolean,
        timeEnabled: Boolean,
        batteryEnabled: Boolean,
        textColor: Int,
        backgroundColor: Int,
        backgroundImage: Uri?,
        animationMode: AnimationMode = AnimationMode.SIMPLE,
        animationSpeed: Float = 0.8f,
        font: Typeface? = null,
        centerPercent: Float = 0.5f,
        /**
         * 这个不支持阅读中修改，省事，
         */
        var fullScreenClickNextPage: Boolean = false
) {
    internal var listeners = mutableListOf<ConfigChangedListener>()

    var textSize: Int by ConfigDelegate(textSize, TextSize)
    var lineSpacing: Int by ConfigDelegate(lineSpacing, LineSpacing)
    var paragraphSpacing: Int  by ConfigDelegate(paragraphSpacing, ParagraphSpacing)

    var contentMargins: IMargins by ConfigDelegate(contentMargins, ContentMargins)

    var paginationMargins: IMargins by ConfigDelegate(paginationMargins, PaginationMargins)

    var timeMargins: IMargins by ConfigDelegate(timeMargins, TimeMargins)

    var batteryMargins: IMargins by ConfigDelegate(batteryMargins, BatteryMargins)

    var bookNameMargins: IMargins by ConfigDelegate(bookNameMargins, BookNameMargins)

    var chapterNameMargins: IMargins by ConfigDelegate(chapterNameMargins, ChapterNameMargins)


    var paginationEnabled: Boolean by ConfigDelegate(paginationEnabled, PaginationEnabled)

    var timeEnabled: Boolean by ConfigDelegate(timeEnabled, TimeEnabled)

    var batteryEnabled: Boolean by ConfigDelegate(batteryEnabled, BatteryEnabled)

    var bookNameEnabled: Boolean by ConfigDelegate(bookNameEnabled, BookNameEnabled)

    var chapterNameEnabled: Boolean by ConfigDelegate(chapterNameEnabled, ChapterNameEnabled)

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
