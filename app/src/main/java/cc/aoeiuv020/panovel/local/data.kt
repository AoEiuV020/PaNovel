@file:Suppress("MemberVisibilityCanPrivate")

package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.panovel.api.NovelItem
import java.util.*

/**
 *
 * Created by AoEiuV020 on 2017.10.04-21:13:36.
 */
abstract class LocalData

data class NovelProgress(var chapter: Int = 0, var text: Int = 0)
    : LocalData()

/**
 * 这个纠结，有的地方强行用了这个，date多余，但不用影响继承关系，
 */
data class NovelHistory(val novel: NovelItem, val date: Date = Date(0))
    : LocalData()

data class NovelId(val site: String, val author: String, val name: String)
    : LocalData() {
    override fun toString(): String = "$name.$author.$site"
}

data class BookListData(val name: String, val list: MutableSet<NovelItem> = mutableSetOf())
    : LocalData()