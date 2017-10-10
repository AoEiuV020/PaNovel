package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.panovel.api.GsonSerializable
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.NovelDetail
import java.util.*

/**
 *
 * Created by AoEiuV020 on 2017.10.04-21:13:36.
 */
abstract class LocalData : GsonSerializable

data class NovelProgress(var chapterProgress: Int = 0, var textProgress: Int = 0)
    : LocalData()

data class NovelHistory(val detail: NovelDetail, val date: Date = Date())
    : LocalData()

data class NovelChapters(val chapters: List<NovelChapter>)
    : LocalData()