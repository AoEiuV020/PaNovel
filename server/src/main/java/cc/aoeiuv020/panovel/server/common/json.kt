package cc.aoeiuv020.panovel.server.common

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 *
 * Created by AoEiuV020 on 2018.04.05-10:35:13.
 */
val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        .create()

inline fun <reified T> type(): Type = object : TypeToken<T>() {}.type
inline fun <reified T> String.toBean(): T = toBean(type<T>())
fun <T> String.toBean(type: Type): T = gson.fromJson(this, type)
        ?: throw IllegalStateException("empty json")

fun Any.toJson(): String = gson.toJson(this)
