package cc.aoeiuv020.irondb

import cc.aoeiuv020.base.jar.type
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
