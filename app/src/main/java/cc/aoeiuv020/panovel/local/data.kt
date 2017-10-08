package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.panovel.api.GsonSerializable

/**
 *
 * Created by AoEiuV020 on 2017.10.04-21:13:36.
 */
abstract class LocalData : GsonSerializable

data class NovelProgress(var chapterProgress: Int = 0, var textProgress: Int = 0)
    : LocalData()