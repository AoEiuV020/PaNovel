package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.panovel.api.NovelDetail
import java.io.Serializable

/**
 *
 * Created by AoEiuV020 on 2017.10.04-21:13:36.
 */
abstract class LocalData : Serializable

data class NovelLocal(val novelDetail: NovelDetail, val progress: NovelProgress = NovelProgress())
    : LocalData()

data class NovelProgress(var chapterProgress: Int = 0, var textProgress: Int = 0)
    : LocalData()