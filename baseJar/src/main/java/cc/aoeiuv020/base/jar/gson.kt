package cc.aoeiuv020.base.jar

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 * Created by AoEiuV020 on 2018.05.13-13:27:12.
 */
inline fun <reified T> type(): Type = object : TypeToken<T>() {}.type

fun Any.toJson(gson: Gson): String = gson.toJson(this)
// reified T 可以直接给gson用，没有reified的T用TypeToken包装也没用，只能传入type,
inline fun <reified T> String.toBean(gson: Gson): T = toBean(gson, type<T>())

fun <T> String.toBean(gson: Gson, type: Type): T = gson.fromJson<T>(this, type)
