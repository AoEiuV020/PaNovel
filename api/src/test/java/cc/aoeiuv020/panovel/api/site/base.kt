package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.NovelContext
import org.junit.Assert.*
import org.junit.Before
import java.text.SimpleDateFormat
import java.util.*
import kotlin.reflect.KClass

/**
 * Created by AoEiuV020 on 2018.05.21-19:15:56.
 */
abstract class BaseNovelContextText(private val clazz: KClass<out NovelContext>) {
    init {
        System.setProperty("org.slf4j.simpleLogger.log.${clazz.java.simpleName}", "trace")
    }

    protected lateinit var context: NovelContext
    @Before
    fun setUp() {
        context = clazz.java.newInstance()
    }

    protected fun search(name: String, count: Int = 3) {
        val list = context.searchNovelName(name)
        println(list.size)
        list.take(count).forEach {
            println(it)
        }
        assertTrue(list.any { it.name.contains(name) })
    }

    protected fun detail(detailExtra: String, name: String, author: String, extra: String, image: String, intro: String? = null, update: String? = null) {
        val detail = context.getNovelDetail(detailExtra)
        println(detail)
        assertEquals(name, detail.novel.name)
        assertEquals(author, detail.novel.author)
        assertEquals(image, detail.bigImg)
        assertEquals(intro, detail.introduction)
        assertEquals(extra, detail.extra)
        compareUpdate(update, detail.update)
    }

    private val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    /**
     * @return 返回true表示对比通过，actual比once时间更晚，
     */
    private fun compareUpdate(once: String?, actual: Date?): Boolean {
        if (once == null) {
            assertNull(actual)
        } else {
            val update = requireNotNull(actual)
            println(sdf.format(update))
            assertTrue(update >= sdf.parse(once))
        }
        return true
    }

    protected fun chapters(extra: String,
                           firstName: String, firstExtra: String, firstUpdate: String?,
                           lastName: String, lastExtra: String, lastUpdate: String?,
                           size: Int,
                           count: Int = 3) {
        val list = context.getNovelChaptersAsc(extra)
        println(list.size)
        list.take(count).forEach {
            println(it)
        }
        val first = list.first()
        assertEquals(firstName, first.name)
        assertEquals(firstExtra, first.extra)
        compareUpdate(firstUpdate, first.update)
        val newList = list.takeLastWhile {
            println(it)
            it.name != lastName || it.extra != lastExtra
        }
        val last = list[list.size - newList.size - 1]
        assertEquals(lastName, last.name)
        assertEquals(lastExtra, last.extra)
        // 可能有的网站只有最新章节有更新时间，
        compareUpdate(lastUpdate, last.update ?: newList.lastOrNull()?.update)
        assertTrue(size == list.size - newList.size)
    }

    protected fun content(extra: String,
                          firstLine: String,
                          lastLine: String,
                          size: Int) {
        val novelText = context.getNovelText(extra)
        val list = novelText.textList
        println(list.size)
        assertEquals(firstLine, list.first())
        assertEquals(lastLine, list.last())
        assertEquals(size, list.size)
    }

}
