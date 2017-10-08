package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.panovel.api.GsonSerializable
import java.io.Serializable
import kotlin.reflect.KProperty

/**
 *
 * Created by AoEiuV020 on 2017.10.07-22:48:22.
 */

/**
 * 只用在原始类型，
 * 不要用在自定义的Serializable类，
 * 非空，
 */
class PrimitiveDelegate<T : Serializable>(private val default: T) {
    operator fun getValue(thisRef: LocalSource, property: KProperty<*>): T {
        this::class.java.isPrimitive
        return thisRef.primitiveLoad(property.name) ?: default
    }

    operator fun setValue(thisRef: LocalSource, property: KProperty<*>, value: T) {
        thisRef.primitiveSave(property.name, value)
    }
}

/**
 * 只用在原始类型，
 * 不要用在自定义的Serializable类，
 * 可空，
 */
@Suppress("unused")
class NullablePrimitiveDelegate<T : Serializable>(private val default: T? = null) {
    operator fun getValue(thisRef: LocalSource, property: KProperty<*>): T? {
        return thisRef.primitiveLoad(property.name) ?: default
    }

    operator fun setValue(thisRef: LocalSource, property: KProperty<*>, value: T?) {
        thisRef.primitiveSave(property.name, value)
    }
}

/**
 * 用gson序列化对象储存json字符串，
 * 可空，
 */
class GsonDelegate<T : GsonSerializable>(private val default: T? = null, private val type: Class<T>) {
    companion object {
        inline fun <reified T : GsonSerializable> new(default: T? = null) = GsonDelegate(default, T::class.java)
    }

    operator fun getValue(thisRef: LocalSource, property: KProperty<*>): T? {
        return thisRef.gsonLoad(property.name, type) ?: default
    }

    operator fun setValue(thisRef: LocalSource, property: KProperty<*>, value: T?) {
        thisRef.gsonSave(property.name, value)
    }
}

