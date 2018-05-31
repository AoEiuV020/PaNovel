package cc.aoeiuv020.panovel.server.service.impl

import cc.aoeiuv020.panovel.server.ServerAddress
import cc.aoeiuv020.panovel.server.dal.model.autogen.Novel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by AoEiuV020 on 2018.04.13-10:19:51.
 */
class NovelServiceImplTest {
    private lateinit var service: NovelServiceImpl
    @Before
    fun setUp() {
        service = NovelServiceImpl(ServerAddress.new("localhost:8080"))
    }

    @Test
    fun uploadUpdate() {
        val novel = Novel().apply {
            site = "Site"
            author = "Author"
            name = "Name"
            detail = "Detail"
            chaptersCount = 12
            receiveUpdateTime = Date()
        }
        assertTrue(service.uploadUpdate(novel))
        novel.receiveUpdateTime = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1))
        assertFalse(service.uploadUpdate(novel))
        novel.let {
            it.chaptersCount = 13
            it.receiveUpdateTime = null
        }
        assertTrue(service.uploadUpdate(novel))
    }

    @Test
    fun needRefreshNovelList() {
        service.needRefreshNovelList(3).forEach { novel ->
            println("<${novel.run { "$site.$author.$name" }}>")
        }
    }

}