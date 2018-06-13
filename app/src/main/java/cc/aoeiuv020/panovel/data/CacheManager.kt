package cc.aoeiuv020.panovel.data

import android.content.Context
import cc.aoeiuv020.irondb.Database
import cc.aoeiuv020.irondb.Iron
import cc.aoeiuv020.irondb.read
import cc.aoeiuv020.irondb.write
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.data.entity.Novel
import java.util.*

/**
 * Created by AoEiuV020 on 2018.05.23-22:08:14.
 */
class CacheManager(ctx: Context) {
    // 所有都保存在/data/data/cc.aoeiuv020.panovel/cache/novel
    private val root = Iron.db(ctx.cacheDir).sub(KEY_NOVEL)

    private val contentDBMap = WeakHashMap<Long, Database>()
    private fun getContentDB(novel: Novel) = contentDBMap.getOrPut(novel.nId) {
        root.sub(novel.site).sub(novel.author).sub(novel.name).sub(KEY_CONTENT)
    }

    private val chaptersDBMap = WeakHashMap<Long, Database>()
    private fun getChaptersDB(novel: Novel): Database = chaptersDBMap.getOrPut(novel.nId) {
        root.sub(novel.site).sub(novel.author).sub(novel.name).sub(KEY_CHAPTERS)
    }

    fun saveChapters(novel: Novel, list: List<NovelChapter>) {
        getChaptersDB(novel).write(novel.nChapters, list)
    }

    fun loadChapters(novel: Novel): List<NovelChapter>? {
        return getChaptersDB(novel).read(novel.nChapters)
    }

    fun saveContent(novel: Novel, chapter: NovelChapter, text: List<String>) {
        getContentDB(novel).write(chapter.extra, text)
    }

    fun loadContent(novel: Novel, chapter: NovelChapter): List<String>? {
        return getContentDB(novel).read(chapter.extra)
    }


    fun novelContentCached(novel: Novel): Collection<String> {
        return getContentDB(novel).keysContainer()
    }

    fun cleanAll() {
        root.drop()
    }

    fun clean(novel: Novel) {
        getChaptersDB(novel).drop()
        getContentDB(novel).drop()
    }

    companion object {
        const val KEY_NOVEL = "novel"
        const val KEY_CHAPTERS = "chapters"
        const val KEY_CONTENT = "content"
    }
}