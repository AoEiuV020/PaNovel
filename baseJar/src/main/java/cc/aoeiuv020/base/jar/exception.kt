package cc.aoeiuv020.base.jar

/**
 * Created by AoEiuV020 on 2018.06.15-21:48:29.
 */
/**
 * 有时候需要主动中断当前操作，直接抛异常，
 */
fun interrupt(message: String): Nothing = throw IllegalStateException(message)
