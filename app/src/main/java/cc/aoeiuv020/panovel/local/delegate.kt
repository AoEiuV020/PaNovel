package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.panovel.api.ListRequester
import cc.aoeiuv020.panovel.api.NovelGenre
import cc.aoeiuv020.panovel.api.NovelSite
import cc.aoeiuv020.panovel.api.Requester
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.io.Serializable
import kotlin.reflect.KProperty

/**
 *
 * Created by AoEiuV020 on 2017.10.07-22:48:22.
 */
class ContextDelegate<T : Serializable>(private val default: T) {
    operator fun getValue(thisRef: LocalSource, property: KProperty<*>): T {
        return thisRef.fileLoad(property.name) ?: default
    }

    operator fun setValue(thisRef: LocalSource, property: KProperty<*>, value: T) {
        thisRef.fileSave(property.name, value)
    }
}

abstract class PrefDelegate<T : Serializable> {
    operator fun getValue(thisRef: LocalSource, property: KProperty<*>): T? {
        return try {
            fromMap(thisRef.prefLoad(property.name))
        } catch (_: Exception) {
            null
        }
    }

    operator fun setValue(thisRef: LocalSource, property: KProperty<*>, value: T?) {
        if (value == null) {
            thisRef.prefRemove(property.name)
        } else {
            thisRef.prefSave(property.name, toMap(value))
        }
    }

    abstract fun fromMap(map: Map<String, String>): T?
    abstract fun toMap(value: T): Map<String, String>
}

@Suppress("unused")
class NullableContextDelegate<T : Serializable>(private val default: T? = null) {
    operator fun getValue(thisRef: LocalSource, property: KProperty<*>): T? {
        return thisRef.fileLoad(property.name) ?: default
    }

    operator fun setValue(thisRef: LocalSource, property: KProperty<*>, value: T?) {
        thisRef.fileSave(property.name, value)
    }
}

class NovelGenreDelegate : PrefDelegate<NovelGenre>() {
    override fun fromMap(map: Map<String, String>): NovelGenre? {
        return NovelGenre(map.getValue("name"), Requester.deserialize<ListRequester>(map.getValue("requester")))
    }

    override fun toMap(value: NovelGenre): Map<String, String> = value.run {
        mapOf("name" to name, "requester" to Requester.serialize(requester))
    }
}

class NovelSiteDelegate : PrefDelegate<NovelSite>() {
    override fun fromMap(map: Map<String, String>): NovelSite? {
        AnkoLogger<NovelSiteDelegate>().info {
            val stack = Thread.currentThread().stackTrace
            stack.drop(2).take(6).joinToString("\n", "stack trace\n") {
                "\tat ${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})"
            }
        }
        return NovelSite(map.getValue("name"), map.getValue("baseUrl"), map.getValue("logo"))
    }

    override fun toMap(value: NovelSite): Map<String, String> = value.run {
        AnkoLogger<NovelSiteDelegate>().info {
            "$value"
        }
        mapOf("name" to name, "logo" to logo, "baseUrl" to baseUrl).also {
            AnkoLogger<NovelSiteDelegate>().info {
                "$it"
            }
        }
    }
}
