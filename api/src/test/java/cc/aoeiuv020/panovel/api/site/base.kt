package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.anull.notNull
import cc.aoeiuv020.jsonpath.JsonPathUtils
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.NovelContext
import cc.aoeiuv020.panovel.api.NovelDetail
import cc.aoeiuv020.panovel.api.NovelItem
import org.junit.Assert.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.reflect.KClass

/**
 * Created by AoEiuV020 on 2018.05.21-19:15:56.
 */
// 传入class为了在初始化logger前配置log级别，
@Suppress("MemberVisibilityCanBePrivate")
abstract class BaseNovelContextText(clazz: KClass<out NovelContext>) {
    @Suppress("MemberVisibilityCanBePrivate")
    protected val context: NovelContext = clazz.java.newInstance()
    protected open var enabled = context.enabled
    protected val folder: File = File(System.getProperty("java.io.tmpdir", "."))
            .resolve("PaNovel")
            .resolve("api")
            .resolve("test")
            .also { it.mkdirs() }
    protected val cacheDir = folder.resolve("cache")
    protected val filesDir = folder.resolve("files")

    init {
        System.setProperty("org.slf4j.simpleLogger.log.${clazz.java.simpleName}", "trace")
        JsonPathUtils.initGson()
        NovelContext.cache(cacheDir)
        NovelContext.files(filesDir)
    }

    protected fun search(name: String, author: String, extra: String, count: Int = 3): NovelItem? {
        if (context.hide || !enabled) return null
        val list = context.searchNovelName(name)
        println(list.size)
        list.take(count).forEach {
            println(it)
        }
        return list.first { it.name == name && it.author == author && it.extra == extra }
    }

    protected fun search(name: String, count: Int = 3): List<NovelItem>? {
        if (context.hide || !enabled) return null
        val list = context.searchNovelName(name)
        println(list.size)
        list.take(count).forEach {
            println(it)
        }
        assertTrue(list.any { it.name.contains(name) })
        return list
    }

    protected fun detail(detailExtra: String, extra: String, name: String, author: String,
                         image: String?, intro: String? = null, update: String? = null): NovelDetail? {
        if (context.hide || !enabled) return null
        val detail = context.getNovelDetail(detailExtra)
        println(detail)
        assertEquals(name, detail.novel.name)
        assertEquals(author, detail.novel.author)
        assertEquals(detailExtra, detail.novel.extra)
        assertEquals(image, detail.image)
        assertEquals(intro, detail.introduction)
        assertEquals(extra, detail.extra)
        compareUpdate(update, detail.update)
        return detail
    }

    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    /**
     * @return 返回true表示对比通过，actual比once时间更晚，
     */
    private fun compareUpdate(once: String?, actual: Date?): Boolean {
        if (once == null) {
            assertNull(actual)
        } else {
            val update = requireNotNull(actual) {
                "没有更新时间，"
            }
            println(sdf.format(update))
            assertTrue(update >= sdf.parse(once))
        }
        return true
    }

    protected fun chapters(extra: String,
                           firstName: String, firstExtra: String, firstUpdate: String?,
                           lastName: String, lastExtra: String, lastUpdate: String?,
                           size: Int,
                           count: Int = 3): List<NovelChapter>? {
        if (context.hide || !enabled) return null
        val list = context.getNovelChaptersAsc(extra)
        println(list.size)
        list.take(count).forEach {
            println(it)
        }
        val newList = list.takeLastWhile {
            // 至少打印最后一章，
            println(it)
            it.name != lastName || it.extra != lastExtra
        }
        require(newList.size < list.size) {
            "最新章《$lastName》不存在，"
        }
        val first = list.first()
        assertEquals(firstName, first.name)
        assertEquals(firstExtra, first.extra)
        compareUpdate(firstUpdate, first.update)
        val last = list[list.size - newList.size - 1]
        assertEquals(lastName, last.name)
        assertEquals(lastExtra, last.extra)
        // 可能有的网站只有最新章节有更新时间，
        compareUpdate(lastUpdate, last.update ?: newList.lastOrNull()?.update)
        assertEquals(size, list.size - newList.size)
        return list
    }

    protected fun content(extra: String,
                          firstLine: String,
                          lastLine: String,
                          size: Int, count: Int = 3): List<String>? {
        if (context.hide || !enabled) return null
        val list = content(extra).notNull()
        println(list.size)
        list.take(count).forEach {
            println(it)
        }
        list.takeLast(maxOf(0, minOf(count, list.size - count))).forEach {
            println(it)
        }
        assertEquals(firstLine, list.first())
        assertEquals(lastLine, list.last())
        assertEquals(size, list.size)
        return list
    }

    protected fun content(extra: String): List<String>? {
        if (context.hide || !enabled) return null
        return context.getNovelContent(extra) { c, t ->
            println("$c/$t")
        }
    }

}
