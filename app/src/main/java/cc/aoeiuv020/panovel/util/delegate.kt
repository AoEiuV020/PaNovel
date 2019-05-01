@file:Suppress("DEPRECATION")

package cc.aoeiuv020.panovel.util

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.preference.PreferenceFragment
import cc.aoeiuv020.anull.notNull
import cc.aoeiuv020.gson.GsonUtils
import cc.aoeiuv020.gson.toBean
import cc.aoeiuv020.gson.toJson
import cc.aoeiuv020.panovel.App
import com.google.gson.Gson
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import java.io.File
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
    val name: String
    val ctx: Context
        get() = App.ctx
    val sharedPreferencesName: String
        get() = App.ctx.packageName + "_$name"
    val sharedPreferences: SharedPreferences
        get() = App.ctx.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
}

abstract class SubPref(
        pref: Pref,
        subName: String
) : Pref {
    override val name: String = pref.name + "_$subName"
}

/**
 * 给这个设置页面绑定这个Pref,
 * 但是默认值没法处理，两边都要写，
 */
fun PreferenceFragment.attach(pref: Pref) {
    preferenceManager.sharedPreferencesName = pref.sharedPreferencesName
}

/**
 * 所有Delegate从这里获取，
 */
@Suppress("unused")
object Delegates {
    fun string(default: kotlin.String, key: kotlin.String? = null) =
            PrefDelegate.String(default, key)

    fun int(default: Int, key: String? = null) =
            PrefDelegate.Int(default, key)

    // 尽量用int，没什么必要long,
    fun long(default: Long, key: String? = null) =
            PrefDelegate.Long(default, key)

    fun float(default: Float, key: String? = null) =
            PrefDelegate.Float(default, key)

    fun boolean(default: Boolean, key: String? = null) =
            PrefDelegate.Boolean(default, key)

    /**
     * enum枚举保存字符串，读取时用gson解析，
     * 不带引号的字符串也能用gson解析，
     */
    inline fun <reified T : Enum<*>> enum(default: T, key: kotlin.String? = null) =
            PrefDelegate.Enum.new(default, key)

    inline fun <reified T : kotlin.Any> any(default: T, key: kotlin.String? = null) =
            PrefDelegate.Any.new(default, key)

    fun uri(key: String? = null) = UriDelegate(key)
}

/**
 * 文件相关的用这个，
 * Uri可以从文件得到，也可以打开写入文件，
 * ${ctx.cacheDir}/UriDelegate/${pref.name}/${key ?: property.name}
 */
class UriDelegate(
        private val key: kotlin.String? = null
) : ReadWriteProperty<Pref, android.net.Uri?> {
    companion object {
        private const val KEY_URI_DELEGATE = "UriDelegate"
    }

    private fun getFile(thisRef: Pref, property: KProperty<*>): File {
        return App.ctx.filesDir.resolve(KEY_URI_DELEGATE)
                .resolve(thisRef.name)
                .apply { mkdirs() }
                .resolve(key ?: property.name)
    }

    private var backField: Uri? = null
    override fun getValue(thisRef: Pref, property: KProperty<*>): android.net.Uri? {
        if (backField != null) {
            return backField
        }
        val file = getFile(thisRef, property)
        if (!file.exists()) {
            return null
        }
        backField = Uri.fromFile(file)
        return backField
    }

    override fun setValue(thisRef: Pref, property: KProperty<*>, value: android.net.Uri?) {
        if (getValue(thisRef, property) == value) {
            return
        }
        // 先赋值为空，之后通过getValue拿uri, 因为有个判断，这个不为空就拿不到文件，
        backField = null
        val file = getFile(thisRef, property)
        if (value == null) {
            if (!file.delete()) {
                throw Exception("delete failed,")
            }
        } else {
            file.outputStream().use { output ->
                App.ctx.contentResolver.openInputStream(value).notNull().use { input ->
                    input.copyTo(output)
                }
                output.flush()
            }

            backField = getValue(thisRef, property)
        }
    }
}

/**
 * 一个Delegate只用在一个字段，
 * 只用在原始类型，
 * 不要用在自定义的Serializable类，
 * 非空，
 *
 * @param key 如果key为空，直接用成员变量名，不受混淆影响，
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
            return sp.getString(key, default).notNull()
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

    class Enum<T : kotlin.Enum<*>>(
            private val default: T,
            key: kotlin.String? = null,
            private val type: Class<T>
    ) : PrefDelegate<T>(key) {
        companion object {
            val gson: Gson = GsonUtils.gson

            inline fun <reified T : kotlin.Enum<*>> new(default: T, key: kotlin.String? = null) = Enum(default, key, T::class.java)
        }

        override fun getValue(sp: SharedPreferences, key: kotlin.String): T {
            // 没有引号的字符串gson也可以解析的，
            return sp.getString(key, null)?.toBean(gson, type) ?: default
        }

        override fun setValue(editor: SharedPreferences.Editor, key: kotlin.String, value: T) {
            // 不能用gson转String, 会带上引号“，
            editor.putString(key, value.toString())
        }
    }

    /**
     * 读写用gson转成字符串，非空，
     *
     * T要指定为kotlin.Any，否则会被当成可空，
     */
    class Any<T : kotlin.Any>(
            private val default: T,
            key: kotlin.String? = null,
            private val type: Class<T>
    ) : PrefDelegate<T>(key) {
        companion object {
            val gson: Gson = GsonUtils.gson

            inline fun <reified T : kotlin.Any> new(default: T, key: kotlin.String? = null) = Any(default, key, T::class.java)
        }

        override fun getValue(sp: SharedPreferences, key: kotlin.String): T {
            return sp.getString(key, null)?.toBean(gson, type) ?: default
        }

        override fun setValue(editor: SharedPreferences.Editor, key: kotlin.String, value: T) {
            editor.putString(key, value.toJson(gson))
        }
    }
}

