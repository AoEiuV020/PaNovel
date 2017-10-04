package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.panovel.api.NovelGenre
import cc.aoeiuv020.panovel.api.NovelSite

/**
 *
 * Created by AoEiuV020 on 2017.10.04-14:04:44.
 */
object Settings {
    var textSize: Int by ContextDelegate(18)
    var genre: NovelGenre? by NullableContextDelegate()
    var site: NovelSite? by NullableContextDelegate()
}

