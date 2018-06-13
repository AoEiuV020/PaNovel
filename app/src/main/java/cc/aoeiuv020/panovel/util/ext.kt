package cc.aoeiuv020.panovel.util

import cc.aoeiuv020.base.jar.type
import cc.aoeiuv020.panovel.report.Reporter

/**
 * Created by AoEiuV020 on 2018.05.24-12:22:17.
 */

inline fun <reified T> T?.notNullOrReport(): T =
        notNullOrReport(type<T>().toString())

fun <T> T?.notNullOrReport(value: String): T =
        Reporter.notNullOrReport(this, value)
