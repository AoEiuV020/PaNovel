package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.panovel.api.NovelChapter
import java.util.*

data class LocalNovelInfo(
        val type: LocalNovelType,
        var author: String? = null,
        var name: String? = null,
        var introduction: String? = null,
        var chapters: LinkedList<NovelChapter>? = null
)