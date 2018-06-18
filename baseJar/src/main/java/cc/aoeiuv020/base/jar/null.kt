@file:Suppress("unused")

package cc.aoeiuv020.base.jar

/**
 * Created by AoEiuV020 on 2018.06.02-17:41:27.
 */
inline fun <reified T : Any> T?.notNull(): T =
        notNull(type<T>().toString())

fun <T : Any> T?.notNull(value: String): T = requireNotNull(this) {
    "Required $value was null."
}

inline fun <T> tryOrNul(block: () -> T?): T? = try {
    block()
} catch (e: Exception) {
    null
}

