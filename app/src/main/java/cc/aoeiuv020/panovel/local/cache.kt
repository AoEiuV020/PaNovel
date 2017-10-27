package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.NovelDetail
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.api.NovelText
import java.io.File
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
        private val DEFAULT_FILE_NAME = "default"
        private inline fun <reified T> new(defaultName: String = Cache::class.java.simpleName,
                                           defaultTimeout: Long = Long.MAX_VALUE): Cache<T> {
            val type = type<T>()
            val cacheName = (type as? Class<*>)?.simpleName ?: defaultName
            return Cache(type, cacheName, defaultTimeout)
        }

        val detail: Cache<NovelDetail> = new()

        private val chaptersCacheTimeout: Long = TimeUnit.DAYS.toMillis(1)
        val chapters: Cache<List<NovelChapter>> = new("NovelChapters", chaptersCacheTimeout)

        val text: Cache<NovelText> = new()

        val progress: Cache<NovelProgress> = new()

        val item: Cache<NovelItem> = new()
    }

    private fun folder(id: NovelId) = "$id${File.separatorChar}$cacheName"

    /**
     * 简单的替换解决斜杆/的问题，
     * 虽然说用个靠谱点的encoder比较好，但是保存文件名乱码看不顺眼，
     * 只要对应唯一，不需要能decode,
     */
    private fun fileNameEncode(name: String) = name.replace('/', ' ')

    fun put(item: NovelItem, t: T, fileName: String = DEFAULT_FILE_NAME) = put(item.bookId, t, fileName)
    fun put(id: NovelId, t: T, fileName: String = DEFAULT_FILE_NAME) = gsonSave(fileNameEncode(fileName), t, folder(id))

    /**
     * @param refreshTime 刷新时间，只能取出这个时间后保存的缓存，
     */
    fun get(item: NovelItem, fileName: String = DEFAULT_FILE_NAME, refreshTime: Long = System.currentTimeMillis() - defaultTimeout): T?
            = get(item.bookId, fileName, refreshTime)

    fun get(id: NovelId, fileName: String = DEFAULT_FILE_NAME, refreshTime: Long = System.currentTimeMillis() - defaultTimeout): T?
            = gsonLoad(fileNameEncode(fileName), type, refreshTime, folder(id))

    @Suppress("unused")
    fun exists(id: NovelId, fileName: String = DEFAULT_FILE_NAME) = gsonExists(fileNameEncode(fileName), folder(id))

    fun container(item: NovelItem) = container(item.bookId)
    fun container(id: NovelId) = object : Container {
        private val list = gsonNameList(folder(id))
        override fun contains(name: String): Boolean {
            return list.contains(fileNameEncode(name))
        }
    }
}

interface Container {
    fun contains(name: String): Boolean
}