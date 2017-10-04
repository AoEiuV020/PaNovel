package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.panovel.api.DetailRequester
import cc.aoeiuv020.panovel.api.NovelItem
import java.io.Serializable

/**
 *
 * Created by AoEiuV020 on 2017.10.04-21:13:36.
 */
abstract class LocalData : Serializable

data class NovelLocal(val novelItem: NovelItem, val img: String, val requester: DetailRequester)
    : LocalData()