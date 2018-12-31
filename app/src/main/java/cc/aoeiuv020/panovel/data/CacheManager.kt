package cc.aoeiuv020.panovel.data

import android.content.Context
import cc.aoeiuv020.irondb.Database
import cc.aoeiuv020.irondb.Iron
import cc.aoeiuv020.irondb.read
import cc.aoeiuv020.irondb.write
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.settings.LocationSettings
import org.jetbrains.anko.toast
import java.io.File
import java.util.*

/**
 * Created by AoEiuV020 on 2018.05.23-22:08:14.
 */
class CacheManager(ctx: Context) {
    // 所有都保存在/data/data/cc.aoeiuv020.panovel/cache/novel
    private val root = try {
        Iron.db(File(LocationSettings.cacheLocation)).sub(KEY_NOVEL)
    } catch (e: Exception) {
        Reporter.post("初始化缓存目录<${LocationSettings.cacheLocation}>失败，", e)
        ctx.toast(ctx.getString(R.string.tip_init_cache_failed_place_holder, LocationSettings.cacheLocation))
        // 失败一次就改成默认的，以免反复失败，
        LocationSettings.cacheLocation = ctx.cacheDir.absolutePath
        Iron.db(ctx.cacheDir).sub(KEY_NOVEL)
    }

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

    fun saveContent(novel: Novel, extra: String, text: List<String>) {
        getContentDB(novel).write(extra, text)
    }

    fun loadContent(novel: Novel, extra: String): List<String>? {
        return getContentDB(novel).read(extra)
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