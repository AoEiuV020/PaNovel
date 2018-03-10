package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.panovel.api.NovelItem

/**
 * 阅读进度，
 * Created by AoEiuV020 on 2017.11.20-16:34:02.
 */
object Progress : BaseLocalSource() {
    fun load(novelItem: NovelItem): NovelProgress
            = gsonLoad(novelItem.bookId.toString())
            ?: Cache.progress.get(novelItem) ?: NovelProgress()

    fun save(novelItem: NovelItem, progress: NovelProgress) {
        // 书架外的书的进度保存在缓存里，表示可以随时删除，
        if (Bookshelf.contains(novelItem)) {
            gsonSave(novelItem.bookId.toString(), progress)
        } else {
            Cache.progress.put(novelItem, progress)
        }
    }
}