@file:Suppress("unused")

package cc.aoeiuv020.panovel.settings

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import cc.aoeiuv020.panovel.local.Settings
import cc.aoeiuv020.reader.AnimationMode
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.find


/**
 *
 * Created by AoEiuV020 on 2017.10.15-20:55:31.
 */
class ListPreference : android.preference.ListPreference, AnkoLogger {
    companion object {
        private val map = mapOf<String, Pair<() -> String, (String) -> Unit>>(
                "animation_mode" to ({ Settings.animationMode.toString() } to { v -> Settings.animationMode = AnimationMode.valueOf(v) })
        )
    }

    constructor(context: Context)
            : super(context)

    constructor(context: Context, attrs: AttributeSet?)
            : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int)
            : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        widgetLayoutResource = android.R.layout.simple_list_item_1

        // 默认可能没有读取数据初始化，所以要这句，
        value = getPersistedString(value)
    }

    override fun persistString(string: String): Boolean = try {
        debug { "$key < $string" }
        map[key]?.run { second(string); true } ?: false
    } catch (_: Exception) {
        false
    }

    override fun getPersistedString(defaultReturnValue: String?): String? {
        return (map[key]?.run { first() } ?: defaultReturnValue).also {
            debug { "$key > $it" }
        }
    }

    override fun onBindView(view: View) {
        super.onBindView(view)
        val tv = view.find<TextView>(android.R.id.text1)
        tv.text = entry
    }

}

class EditTextPreference : android.preference.EditTextPreference, AnkoLogger {
    companion object {
        private val map = mapOf<String, Pair<() -> String, (String) -> Unit>>(
                "download_thread_count" to ({ Settings.downloadThreadCount.toString() } to { v -> Settings.downloadThreadCount = v.toInt() }),
                "red_dot_size" to ({ Settings.bookshelfRedDotSize.toString() } to { v -> Settings.bookshelfRedDotSize = v.toFloat() }),
                "async_thread_count" to ({ Settings.asyncThreadCount.toString() } to { v -> Settings.asyncThreadCount = v.toInt() }),
                "history_count" to ({ Settings.historyCount.toString() } to { v -> Settings.historyCount = v.toInt() }),
                "line_spacing" to ({ Settings.lineSpacing.toString() } to { v -> Settings.lineSpacing = v.toInt() }),
                "paragraph_spacing" to ({ Settings.paragraphSpacing.toString() } to { v -> Settings.paragraphSpacing = v.toInt() }),
                "left_spacing" to ({ Settings.leftSpacing.toString() } to { v -> Settings.leftSpacing = v.toInt() }),
                "right_spacing" to ({ Settings.rightSpacing.toString() } to { v -> Settings.rightSpacing = v.toInt() }),
                "top_spacing" to ({ Settings.topSpacing.toString() } to { v -> Settings.topSpacing = v.toInt() }),
                "bottom_spacing" to ({ Settings.bottomSpacing.toString() } to { v -> Settings.bottomSpacing = v.toInt() }),
                "full_screen_delay" to ({ Settings.fullScreenDelay.toString() } to { v -> Settings.fullScreenDelay = v.toInt() }),
                "animation_speed" to ({ Settings.animationSpeed.toString() } to { v -> Settings.animationSpeed = v.toFloat() }),
                "text_size" to ({ Settings.textSize.toString() } to { v -> Settings.textSize = v.toInt() })
        )
    }

    constructor(context: Context)
            : super(context)

    constructor(context: Context, attrs: AttributeSet?)
            : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int)
            : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        widgetLayoutResource = android.R.layout.simple_list_item_1

        // 默认可能没有读取数据初始化，所以要这句，
        text = getPersistedString(text)
    }

    override fun persistString(string: String): Boolean = try {
        debug { "$key < $string" }
        map[key]?.run { second(string); true } ?: false
    } catch (_: Exception) {
        false
    }

    override fun getPersistedString(defaultReturnValue: String?): String? {
        return (map[key]?.run { first() } ?: defaultReturnValue).also {
            debug { "$key > $it" }
        }
    }

    override fun onBindView(view: View) {
        super.onBindView(view)
        val tv = view.find<TextView>(android.R.id.text1)
        tv.text = text
    }
}

class SwitchPreference : android.preference.SwitchPreference, AnkoLogger {
    companion object {
        private val map = mapOf<String, Pair<() -> Boolean, (Boolean) -> Unit>>(
                "auto_save" to ({ Settings.bookListAutoSave } to { v -> Settings.bookListAutoSave = v }),
                "volume_key_scroll" to ({ Settings.volumeKeyScroll } to { v -> Settings.volumeKeyScroll = v }),
                "full_screen_click_next_page" to ({ Settings.fullScreenClickNextPage } to { v -> Settings.fullScreenClickNextPage = v }),
                "auto_refresh" to ({ Settings.bookshelfAutoRefresh } to { v -> Settings.bookshelfAutoRefresh = v }),
                "red_dot_notify_not_read_or_new_chapter" to ({ Settings.bookshelfRedDotNotifyNotReadOrNewChapter } to { v -> Settings.bookshelfRedDotNotifyNotReadOrNewChapter = v }),
                "show_more_action_dot" to ({ Settings.bookshelfShowMoreActionDot } to { v -> Settings.bookshelfShowMoreActionDot = v }),
                "book_small_layout" to ({ Settings.BookSmallLayout } to { v -> Settings.BookSmallLayout = v }),
                "back_press_out_of_fullScreen" to ({ Settings.backPressOutOfFullScreen } to { v -> Settings.backPressOutOfFullScreen = v }),
                "ad_enabled" to ({ Settings.adEnabled } to { v -> Settings.adEnabled = v })
        )
    }

    constructor(context: Context)
            : super(context)

    constructor(context: Context, attrs: AttributeSet?)
            : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int)
            : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        // 默认可能没有读取数据初始化，所以要这句，莫名奇妙的，
        isChecked = getPersistedBoolean(false)
    }

    override fun setChecked(checked: Boolean) {
        debug { "$title set $checked" }
        try {
            map[key]?.run { second(checked) }
        } catch (_: Exception) {
        }
        super.setChecked(checked)
    }

    override fun getPersistedBoolean(defaultReturnValue: Boolean): Boolean {
        val checked = try {
            map[key]?.run { first() } ?: super.getPersistedBoolean(defaultReturnValue)
        } catch (_: Exception) {
            false
        }
        debug { "$title get $checked" }
        return checked
    }
}

class ColorPickerPreference : com.flask.colorpicker.ColorPickerPreference {
    companion object {
        private val map = mapOf<String, Pair<() -> Int, (Int) -> Unit>>(
                "chapter_color_default" to ({ Settings.chapterColorDefault } to { v -> Settings.chapterColorDefault = v }),
                "red_dot_color" to ({ Settings.bookshelfRedDotColor } to { v -> Settings.bookshelfRedDotColor = v }),
                "chapter_color_cached" to ({ Settings.chapterColorCached } to { v -> Settings.chapterColorCached = v }),
                "chapter_color_read_at" to ({ Settings.chapterColorReadAt } to { v -> Settings.chapterColorReadAt = v }),
                "background_color" to ({ Settings.backgroundColor } to { v -> Settings.backgroundColor = v }),
                "text_color" to ({ Settings.textColor } to { v -> Settings.textColor = v })
        )
    }

    constructor(context: Context)
            : super(context)

    constructor(context: Context, attrs: AttributeSet?)
            : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    init {
        // 默认没有读取数据初始化，所以要这句，
        setValue(getPersistedInt(-1))
    }

    override fun persistInt(value: Int): Boolean
            = map[key]?.run { second(value); true } ?: false

    override fun getPersistedInt(defaultReturnValue: Int): Int
            = map[key]?.run { first() } ?: defaultReturnValue
}