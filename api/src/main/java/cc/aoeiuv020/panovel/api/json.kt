package cc.aoeiuv020.panovel.api

import com.google.gson.GsonBuilder

/**
 * Created by AoEiuV020 on 2018.05.19-15:26:46.
 */

fun GsonBuilder.paNovel(): GsonBuilder = apply {
    TODO("NovelItem的解析要自定义，为了兼容旧版，")
    // 不如顺便改了存储，就不用兼容了，
}
