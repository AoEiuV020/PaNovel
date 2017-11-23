package cc.aoeiuv020.panovel.util

import android.content.Context

/**
 * 文件操作相关，
 * Created by AoEiuV020 on 2017.11.23-11:27:40.
 */
fun Context.assetsRead(name: String): String = assets.open(name).reader().readText()