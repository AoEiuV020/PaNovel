package cc.aoeiuv020.irondb

import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 *     var author: String? by root.delegate()
 *
 * Created by AoEiuV020 on 2018.06.12-17:41:37.
 */
@Suppress("unused")
inline fun <reified T> Database.delegate(key: String? = null): ReadWriteProperty<Any, T?> =
        delegate(key, type<T>())

//        delegate(key, cc.aoeiuv020.base.jar.type<T>())
// 不明原因，使用cc.aoeiuv020.base.jar.type会导致查看kotlin bytecode抛异常，但是能正常编译，
// https://youtrack.jetbrains.com/issue/KT-24889
inline fun <reified T> type(): Type = object : TypeToken<T>() {}.type

fun <T> Database.delegate(key: String?, type: Type): ReadWriteProperty<Any, T?> =
        DatabaseProperty(this, key, type)

internal class DatabaseProperty<T>(
        private val database: Database,
        private val key: String?,
        private val type: Type
) : ReadWriteProperty<Any, T?> {
    override fun getValue(thisRef: Any, property: KProperty<*>): T? {
        val realKey = key ?: property.name
        return database.read(realKey, type)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
        val realKey = key ?: property.name
        database.write(realKey, value, type)
    }
}
