@file:Suppress("unused")

package cc.aoeiuv020.panovel.settings

import android.content.Context
import android.content.res.TypedArray
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.find


/**
 *
 * Created by AoEiuV020 on 2017.10.15-20:55:31.
 */
/**
 * 实现控件右边显示当前值，
 */
class ListPreference : android.preference.ListPreference, AnkoLogger {
    constructor(context: Context)
            : super(context)

    constructor(context: Context, attrs: AttributeSet?)
            : super(context, attrs)

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int)
            : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        widgetLayoutResource = android.R.layout.simple_list_item_1

        // 默认可能没有读取数据初始化，所以要这句，
        value = getPersistedString(value)
    }

    override fun onBindView(view: View) {
        super.onBindView(view)
        val tv = view.find<TextView>(android.R.id.text1)
        tv.text = entry
    }
}

/**
 * 实现控件右边显示当前值，
 */
open class EditTextPreference : android.preference.EditTextPreference {

    constructor(context: Context)
            : super(context)

    constructor(context: Context, attrs: AttributeSet?)
            : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int)
            : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        widgetLayoutResource = android.R.layout.simple_list_item_1
    }

    // 自动设置默认值，这里存起来，下面onBindView能拿到，
    override fun onGetDefaultValue(a: TypedArray?, index: Int): Any {
        return super.onGetDefaultValue(a, index).also {
            text = it.toString()
        }
    }

    override fun onBindView(view: View) {
        super.onBindView(view)
        val tv: TextView = view.find(android.R.id.text1)
        tv.text = text
    }
}

/**
 * 实现保存Int的EditTextPreference，
 */
class IntEditTextPreference : EditTextPreference {
    constructor(context: Context)
            : super(context)

    constructor(context: Context, attrs: AttributeSet?)
            : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int)
            : super(context, attrs, defStyleAttr, defStyleRes)


    override fun persistString(string: String): Boolean {
        val value = try {
            // 主要也就是string==""的情况，
            string.toInt()
        } catch (e: NumberFormatException) {
            // 格式出错无视就好，
            return false
        }
        return persistInt(value)
    }

    override fun getPersistedString(defaultReturnValue: String?): String {
        return getPersistedInt(defaultReturnValue?.takeIf(String::isNotEmpty)?.toInt()
                ?: 0).toString()
    }

}

/**
 * 实现保存Float的EditTextPreference，
 */
class FloatEditTextPreference : EditTextPreference {
    constructor(context: Context)
            : super(context)

    constructor(context: Context, attrs: AttributeSet?)
            : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int)
            : super(context, attrs, defStyleAttr, defStyleRes)


    override fun persistString(string: String): Boolean {
        val value = try {
            // 主要也就是string==""的情况，
            string.toFloat()
        } catch (e: NumberFormatException) {
            // 格式出错无视就好，
            return false
        }
        return persistFloat(value)
    }

    override fun getPersistedString(defaultReturnValue: String?): String {
        return getPersistedFloat(defaultReturnValue?.takeIf(String::isNotEmpty)?.toFloat()
                ?: 0f).toString()
    }

}

/**
 * 实现装载默认值，
 */
class ColorPickerPreference : com.flask.colorpicker.ColorPickerPreference {
    constructor(context: Context)
            : super(context)

    constructor(context: Context, attrs: AttributeSet?)
            : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    override fun onGetDefaultValue(a: TypedArray?, index: Int): Any? {
        val defaultString = a?.getString(index) ?: return null
        // 支持大写，
        return defaultString.toLowerCase().let {
            if (it.startsWith("0x")) {
                // 支持16进制，正常手写都是用16进制的，
                // 过大不能直接转Int, 先转Long, 再Int,
                it.removePrefix("0x").toLong(radix = 16)
            } else {
                // 也要支持10进制，修改时保存的是10进制，
                it.toLong()
            }
        }.toInt().also {
            // 调一下才会把默认值显示在右边，
            setValue(it)
        }
    }
}

