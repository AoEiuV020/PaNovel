@file:Suppress("unused")

package cc.aoeiuv020.panovel.settings

import android.content.Context
import android.util.AttributeSet
import cc.aoeiuv020.panovel.local.Settings
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug


/**
 *
 * Created by AoEiuV020 on 2017.10.15-20:55:31.
 */
class EditTextPreference : android.preference.EditTextPreference, AnkoLogger {
    companion object {
        private val map = mapOf<String, Pair<() -> String, (String) -> Unit>>(
                "download_thread_count" to ({ Settings.downloadThreadCount.toString() } to { v -> Settings.downloadThreadCount = v.toInt() }),
                "async_thread_count" to ({ Settings.asyncThreadCount.toString() } to { v -> Settings.asyncThreadCount = v.toInt() }),
                "history_count" to ({ Settings.historyCount.toString() } to { v -> Settings.historyCount = v.toInt() }),
                "line_spacing" to ({ Settings.lineSpacing.toString() } to { v -> Settings.lineSpacing = v.toInt() }),
                "paragraph_spacing" to ({ Settings.paragraphSpacing.toString() } to { v -> Settings.paragraphSpacing = v.toInt() }),
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
}

class SwitchPreference : android.preference.SwitchPreference, AnkoLogger {
    companion object {
        private val map = mapOf<String, Pair<() -> Boolean, (Boolean) -> Unit>>(
                "auto_save" to ({ Settings.bookListAutoSave } to { v -> Settings.bookListAutoSave = v }),
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

    override fun setChecked(checked: Boolean) {
        debug { "$title set $checked" }
        try {
            map[key]?.run { second(checked) }
        } catch (_: Exception) {
        }
        super.setChecked(checked)
    }

    override fun isChecked(): Boolean {
        val checked = try {
            map[key]?.run { first() } ?: false
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