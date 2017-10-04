package cc.aoeiuv020.panovel.local

import android.content.Context
import cc.aoeiuv020.panovel.App
import java.io.*
import kotlin.reflect.KProperty

/**
 *
 * Created by AoEiuV020 on 2017.10.04-15:33:13.
 */

class ContextDelegate<T : Serializable>(private val default: T) {
    private fun name(thisRef: Any, property: KProperty<*>) = "${thisRef.javaClass.name}.${property.name}"
    operator fun getValue(thisRef: Any, property: KProperty<*>): T {
        return try {
            App.ctx.load(name(thisRef, property))!!
        } catch (_: Exception) {
            default
        }
    }

    operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        App.ctx.save(name(thisRef, property), value)
    }
}

class NullableContextDelegate<T : Serializable>(private val default: T? = null) {
    private fun name(thisRef: Any, property: KProperty<*>) = "${thisRef.javaClass.name}.${property.name}"
    operator fun getValue(thisRef: Any, property: KProperty<*>): T? {
        return try {
            App.ctx.load(name(thisRef, property))!!
        } catch (_: Exception) {
            default
        }
    }

    operator fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
        App.ctx.save(name(thisRef, property), value)
    }
}

private fun Context.ext() = File(getExternalFilesDir(null), "ext").apply { mkdirs() }
fun Context.save(name: String, obj: Serializable?) {
    ObjectOutputStream(FileOutputStream(File(ext(), name))).run {
        writeObject(obj)
        close()
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> Context.load(name: String): T? = ObjectInputStream(FileInputStream(File(ext(), name))).run {
    (readObject() as? T).also { close() }
}
