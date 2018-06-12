package cc.aoeiuv020.panovel.data

import cc.aoeiuv020.base.jar.notNull
import cc.aoeiuv020.irondb.Iron
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.nio.charset.Charset

/**
 * Created by AoEiuV020 on 2018.06.12-17:27:05.
 */
class LocalManagerTest {
    private val local = LocalManager(Iron.db(File("/tmp/panovel/test/local")))

    @Test
    fun importText() {
        javaClass.getResourceAsStream("/zxcs.txt").notNull().use { input ->
            local.importText(input, Charset.forName("GBK"))
        }
        assertEquals("卜非", local.author)
        assertEquals("与千年女鬼同居的日子", local.name)
        val chapters = local.chapters.notNull()
        assertEquals(5, chapters.size)
        chapters.first().let {
            assertEquals("第一卷 红衣夜女", it.name)
            val content = local.getContent(it.extra)
            assertEquals(0, content.size)
        }
        chapters[1].let {
            assertEquals("第1章 女鬼来了", it.name)
            val content = local.getContent(it.extra)
            assertEquals("黄昏时分，燕京郊区，西山墓园边的一块规划坟地。", content.first())
            assertEquals("矮坟不大，上面的泥土还散发着一股新鲜的气息，像是最近才翻盖的一般。几根小草从矮坟中冒出了头。", content.last())
            assertEquals(6, content.size)
        }
        chapters.last().let {
            assertEquals("第82章 报仇", it.name)
            val content = local.getContent(it.extra)
            assertEquals("红衣女鬼终于被搞定了，可是，刘浪的心却再也无法平静了。", content.first())
            assertEquals("这阴阳书就是破除韩家诅咒的关键。", content.last())
            assertEquals(5, content.size)
        }
    }
}