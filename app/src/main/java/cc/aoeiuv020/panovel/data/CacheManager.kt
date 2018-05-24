package cc.aoeiuv020.panovel.data

import android.content.Context
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.data.entity.Novel

/**
 * Created by AoEiuV020 on 2018.05.23-22:08:14.
 */
class CacheManager(ctx: Context) {
    fun saveChapters(novel: Novel, list: List<NovelChapter>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun loadChapters(novel: Novel): List<NovelChapter>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun loadContent(novel: Novel, chapter: NovelChapter): List<String>? {
        TODO()
    }

    fun saveContent(novel: Novel, chapter: NovelChapter, text: List<String>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun novelContentCachedSet(novel: Novel): Collection<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}