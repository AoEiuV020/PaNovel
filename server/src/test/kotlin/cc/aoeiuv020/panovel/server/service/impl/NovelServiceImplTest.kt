package cc.aoeiuv020.panovel.server.service.impl

import cc.aoeiuv020.panovel.server.ServerAddress
import cc.aoeiuv020.panovel.server.dal.model.autogen.Novel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by AoEiuV020 on 2018.04.13-10:19:51.
 */
class NovelServiceImplTest {
    init {
        System.setProperty("org.slf4j.simpleLogger.log.NovelServiceImpl", "trace")
    }

    private lateinit var service: NovelServiceImpl

    @Before
    fun setUp() {
        service = NovelServiceImpl(ServerAddress.getDefault())
    }

    @Test
    fun touch() {
        val novel = Novel().apply {
            site = "起点中文"
            author = "圣骑士的传说"
            name = "修真聊天群"
            detail = "3602691"
            chaptersCount = 13
            checkUpdateTime = Date()
            receiveUpdateTime = checkUpdateTime
        }
        assertFalse(service.touch(novel))
    }

    @Test
    fun delete() {
        val novel = Novel().apply {
            site = "起点中文"
            author = "圣骑士的传说"
            name = "修真聊天群"
            detail = "3602691"
            chaptersCount = 12
            checkUpdateTime = Date()
            receiveUpdateTime = Date(checkUpdateTime.time - TimeUnit.DAYS.toMillis(4))
        }
        assertFalse(service.touch(novel))
    }

    @Test
    fun uploadUpdate() {
        val novel = Novel().apply {
            site = "起点中文"
            author = "圣骑士的传说"
            name = "修真聊天群"
            detail = "3602691"
            chaptersCount = 12
            checkUpdateTime = Date()
            receiveUpdateTime = checkUpdateTime
        }
        assertFalse(service.uploadUpdate(novel))
        novel.chaptersCount = 22222
        assertTrue(service.uploadUpdate(novel))
    }

    @Test
    fun queryTest() {
        val novel = Novel().apply {
            site = "起点中文"
            author = "圣骑士的传说"
            name = "修真聊天群"
            detail = "3602691"
            chaptersCount = 12
            receiveUpdateTime = Date()
        }
        val novelMap = mapOf(8L to novel)
        val resultMap = service.queryList(novelMap)
        val result = novelMap.mapNotNull { (id, novel) ->
            val response = resultMap[id] ?: return@mapNotNull null
            println(response)
            novel.checkUpdateTime = response.checkUpdateTime
            novel
        }.first()
        requireNotNull(result)
        assertEquals(novel.site, result.site)
        assertEquals(novel.author, result.author)
        assertEquals(novel.name, result.name)
        assertEquals(novel.detail, result.detail)
        println(result.receiveUpdateTime)
        println(result.checkUpdateTime)
    }

    @Test
    fun needRefreshNovelList() {
        service.needRefreshNovelList(10).forEach { novel ->
            println("<${novel.run { "$site.$author.$name/$chaptersCount" }}>")
        }
    }

    @Test
    fun version() {
        service.minVersion().let { println(it) }
    }

    @Test
    fun message() {
        service.message().let { println(it) }
    }

}