@file:Suppress("unused")

package cc.aoeiuv020.panovel.api

import com.google.gson.GsonBuilder

/**
 * Created by AoEiuV020 on 2017.10.02-16:01:09.
 */

fun GsonBuilder.paNovel(): GsonBuilder = apply {
    Requester.attach(this)
}

@Suppress("FunctionName")
fun NovelContext.NovelSearch(name: String, url: String) = NovelGenre(name, SearchListRequester(url))
