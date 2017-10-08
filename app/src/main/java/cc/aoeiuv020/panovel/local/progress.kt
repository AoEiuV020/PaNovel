package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.panovel.api.NovelItem

/**
 * 阅读进度，
 * Created by AoEiuV020 on 2017.10.07-18:36:12.
 */
object ReadProgress : LocalSource {
    fun put(novelItem: NovelItem, progress: NovelProgress) = gsonSave(novelItem.bookId, progress)

    fun get(novelItem: NovelItem): NovelProgress = gsonLoad(novelItem.bookId) ?: NovelProgress()
}