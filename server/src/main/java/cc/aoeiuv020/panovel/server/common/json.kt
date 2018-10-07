package cc.aoeiuv020.panovel.server.common

import cc.aoeiuv020.gson.GsonUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 *
 * Created by AoEiuV020 on 2018.04.05-10:35:13.
 */
val gson: Gson = GsonUtils.gsonBuilder
        .create()

inline fun <reified T> type(): Type = object : TypeToken<T>() {}.type
inline fun <reified T> String.toBean(): T = toBean(type<T>())
fun <T> String.toBean(type: Type): T = gson.fromJson(this, type)
        ?: throw IllegalStateException("empty json")

fun Any.toJson(): String = gson.toJson(this)
