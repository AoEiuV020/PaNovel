@file:Suppress("unused")

package cc.aoeiuv020.panovel.api

import com.google.gson.GsonBuilder
import java.net.URL

/**
 * Created by AoEiuV020 on 2017.10.02-16:01:09.
 */

fun GsonBuilder.paNovel(): GsonBuilder = apply {
    Requester.attach(this)
}

fun findBookId(url: String): String {
    return URL(url).path.split("/").first {
        try {
            it.toInt()
            true
        } catch (e: NumberFormatException) {
            false
        }
    }
}