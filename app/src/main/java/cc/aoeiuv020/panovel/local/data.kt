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

data class NovelHistory(val novel: NovelItem, val date: Date = Date())
    : LocalData()

data class NovelId(val site: String, val author: String, val name: String)
    : LocalData() {
    override fun toString(): String = "$name.$author.$site"
}
