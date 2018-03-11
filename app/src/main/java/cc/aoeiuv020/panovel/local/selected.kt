package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.panovel.api.NovelGenre
import cc.aoeiuv020.panovel.api.NovelSite

/**
 * 记住的选择，
 * Created by AoEiuV020 on 2017.10.04-20:04:38.
 */
object Selected : BaseLocalSource() {
    var genre: NovelGenre? by NullableGsonDelegate.new()
    var site: NovelSite? by NullableGsonDelegate.new()
}