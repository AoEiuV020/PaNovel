package cc.aoeiuv020.panovel.util

import android.content.Context
import android.content.SharedPreferences
import cc.aoeiuv020.base.jar.toBean
import cc.aoeiuv020.base.jar.toJson
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * SharedPreferences相关Delegate的封装，
 *
 * Created by AoEiuV020 on 2018.05.17-14:58:10.
 */

/**
 * 指定包含一个SharedPreferences，用于Delegate，
 */
interface Pref {
    val sharedPreferences: SharedPreferences
}

fun Pref.getSharedPreferences(ctx: Context, name: String? = null): SharedPreferences {
    val realName = name ?: this.javaClass.simpleName
    return ctx.getSharedPreferences(ctx.packageName + "_$realName", Context.MODE_PRIVATE)
}

/**
 * 所有Delegate从这里获取，
 */
@Suppress("unused")
object PrefDelegates {
    fun string(default: kotlin.String, key: kotlin.String? = null) =
            PrefDelegate.String(default, key)

    fun int(default: Int, key: String? = null) =
            PrefDelegate.Int(default, key)

    fun long(default: Long, key: String? = null) =
            PrefDelegate.Long(default, key)

    fun float(default: Float, key: String? = null) =
            PrefDelegate.Float(default, key)

    fun boolean(default: Boolean, key: String? = null) =
            PrefDelegate.Boolean(default, key)

    inline fun <reified T : kotlin.Any> any(default: T, key: kotlin.String? = null) =
            PrefDelegate.Any.new(default, key)
}

/**
 * 一个Delegate只用在一个字段，
 * 只用在原始类型，
 * 不要用在自定义的Serializable类，
 * 非空，
 *
 * @param key 如果key为空，直接用成员变量名，
 */
sealed class PrefDelegate<T>(
        private val key: kotlin.String?
) : ReadWriteProperty<Pref, T>, AnkoLogger {
    final override fun getValue(thisRef: Pref, property: KProperty<*>): T {
        val realKey = key ?: property.name
        return getValue(thisRef.sharedPreferences, realKey).also {
            debug { "${property.name} > $it" }
        }
    }

    final override fun setValue(thisRef: Pref, property: KProperty<*>, value: T) {
        val realKey = key ?: property.name
        debug { "$realKey < $value" }
        thisRef.sharedPreferences
                .edit()
                .also { setValue(it, realKey, value) }
                .apply()
    }

    abstract fun getValue(sp: SharedPreferences, key: kotlin.String): T
    abstract fun setValue(editor: SharedPreferences.Editor, key: kotlin.String, value: T)

    class String(
            private val default: kotlin.String,
            key: kotlin.String?
    ) : PrefDelegate<kotlin.String>(key) {
        override fun getValue(sp: SharedPreferences, key: kotlin.String): kotlin.String {
            return sp.getString(key, default)
        }

        override fun setValue(editor: SharedPreferences.Editor, key: kotlin.String, value: kotlin.String) {
            editor.putString(key, value)
        }
    }

    class Int(
            private val default: kotlin.Int,
            key: kotlin.String? = null
    ) : PrefDelegate<kotlin.Int>(key) {
        override fun getValue(sp: SharedPreferences, key: kotlin.String): kotlin.Int {
            return sp.getInt(key, default)
        }

        override fun setValue(editor: SharedPreferences.Editor, key: kotlin.String, value: kotlin.Int) {
            editor.putInt(key, value)
        }
    }

    class Long(
            private val default: kotlin.Long,
            key: kotlin.String? = null
    ) : PrefDelegate<kotlin.Long>(key) {
        override fun getValue(sp: SharedPreferences, key: kotlin.String): kotlin.Long {
            return sp.getLong(key, default)
        }

        override fun setValue(editor: SharedPreferences.Editor, key: kotlin.String, value: kotlin.Long) {
            editor.putLong(key, value)
        }
    }

    class Float(
            private val default: kotlin.Float,
            key: kotlin.String? = null
    ) : PrefDelegate<kotlin.Float>(key) {
        override fun getValue(sp: SharedPreferences, key: kotlin.String): kotlin.Float {
            return sp.getFloat(key, default)
        }

        override fun setValue(editor: SharedPreferences.Editor, key: kotlin.String, value: kotlin.Float) {
            editor.putFloat(key, value)
        }
    }

    class Boolean(
            private val default: kotlin.Boolean,
            key: kotlin.String? = null
    ) : PrefDelegate<kotlin.Boolean>(key) {
        override fun getValue(sp: SharedPreferences, key: kotlin.String): kotlin.Boolean {
            return sp.getBoolean(key, default)
        }

        override fun setValue(editor: SharedPreferences.Editor, key: kotlin.String, value: kotlin.Boolean) {
            editor.putBoolean(key, value)
        }
    }

    /**
     * 读写用gson转成字符串，非空，
     *
     * T要指定为kotlin.Any，否则会被当成可空，
     */
    class Any<T : kotlin.Any>(
            private val defaultString: kotlin.String,
            key: kotlin.String? = null,
            private val type: Class<T>
    ) : PrefDelegate<T>(key) {
        companion object {
            val gson: Gson = GsonBuilder()
                    .create()

            inline fun <reified T : kotlin.Any> new(default: T, key: kotlin.String? = null) = Any(default, key, T::class.java)
        }

        constructor(
                default: T,
                key: kotlin.String?,
                type: Class<T>
        ) : this(default.toJson(gson), key, type)

        override fun getValue(sp: SharedPreferences, key: kotlin.String): T {
            return sp.getString(key, defaultString).toBean(gson, type)
        }

        override fun setValue(editor: SharedPreferences.Editor, key: kotlin.String, value: T) {
            editor.putString(key, value.toJson(gson))
        }
    }
}

