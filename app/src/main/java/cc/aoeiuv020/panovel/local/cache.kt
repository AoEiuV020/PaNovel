package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.NovelDetail
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.api.NovelText
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

/**
 * Cache/gson/bookId/cacheName/fileName
 * default Cache/gson/bookId/T::class.java.simpleName/default
 * Created by AoEiuV020 on 2017.10.10-19:20:47.
 */
class Cache<T>(private val type: Type,
               private val cacheName: String,
               private val defaultTimeout: Long
) : LocalSource {
    companion object : LocalSource {
        private inline fun <reified T> new(defaultName: String = Cache::class.java.simpleName,
                                           defaultTimeout: Long = 0): Cache<T> {
            val type = type<T>()
            val cacheName = (type as? Class<*>)?.simpleName ?: defaultName
            return Cache(type, cacheName, defaultTimeout)
        }

        val detail: Cache<NovelDetail> = new()

        private val chaptersCacheTimeout: Long = TimeUnit.DAYS.toMillis(1)
        val chapters: Cache<List<NovelChapter>> = new("NovelChapters", chaptersCacheTimeout)

        val text: Cache<NovelText> = new()
    }

    private fun id(item: NovelItem, fileName: String) = "${item.bookId}/$cacheName/$fileName"

    fun put(item: NovelItem, t: T, fileName: String = "default") = gsonSave(id(item, fileName), t)

    /**
     * @param timeout 传入0表示不判断超时，
     */
    fun get(item: NovelItem, fileName: String = "default", timeout: Long = defaultTimeout): T? = gsonLoad(id(item, fileName), type, timeout)
}