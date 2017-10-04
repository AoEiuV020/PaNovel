@file:Suppress("unused")

package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.panovel.App
import java.io.*
import kotlin.reflect.KProperty

/**
 *
 * Created by AoEiuV020 on 2017.10.04-15:33:13.
 */

class ContextDelegate<T : Serializable>(private val default: T) {
    operator fun getValue(thisRef: LocalSource, property: KProperty<*>): T {
        return thisRef.fileLoad(property.name) ?: default
    }

    operator fun setValue(thisRef: LocalSource, property: KProperty<*>, value: T) {
        thisRef.fileSave(property.name, value)
    }
}

class NullableContextDelegate<T : Serializable>(private val default: T? = null) {
    operator fun getValue(thisRef: LocalSource, property: KProperty<*>): T? {
        return thisRef.fileLoad(property.name) ?: default
    }

    operator fun setValue(thisRef: LocalSource, property: KProperty<*>, value: T?) {
        thisRef.fileSave(property.name, value)
    }
}

private fun LocalSource.externalFile() = File(File(App.ctx.getExternalFilesDir(null), "ext"), this.javaClass.name).apply { mkdirs() }
fun LocalSource.fileSave(name: String, obj: Serializable?) {
    ObjectOutputStream(FileOutputStream(File(externalFile(), name))).run {
        writeObject(obj)
        close()
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> LocalSource.fileLoad(file: File): T? = try {
    ObjectInputStream(FileInputStream(file)).run {
        (readObject() as? T).also { close() }
    }
} catch (_: Exception) {
    null
}

fun <T> LocalSource.fileLoad(name: String): T? = fileLoad(File(externalFile(), name))

fun LocalSource.fileExists(name: String): Boolean = File(externalFile(), name).exists()
fun LocalSource.fileRemove(name: String): Boolean = File(externalFile(), name).delete()
@Suppress("UNCHECKED_CAST")
fun <T : Any> LocalSource.fileList(): List<T> = externalFile().listFiles().mapNotNull { file ->
    fileLoad<T>(file)
}
