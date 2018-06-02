package cc.aoeiuv020.base.jar

/**
 * Created by AoEiuV020 on 2018.06.02-17:41:27.
 */
inline fun <reified T : Any> T?.notNull(): T = requireNotNull(this)