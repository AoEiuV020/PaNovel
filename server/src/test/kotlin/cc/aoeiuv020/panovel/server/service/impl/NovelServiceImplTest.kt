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
        service = NovelServiceImpl(ServerAddress(
                data = mapOf("updateUploadUrl" to "http://localhost:8080/update/upload")
        ))
    }

    @Test
    fun uploadUpdate() {
        val novel = Novel().also {
            it.requesterType = "type"
            it.requesterExtra = "extra"
            it.chaptersCount = 12
            it.updateTime = Date()
        }
        assertTrue(service.uploadUpdate(novel))
        novel.updateTime = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1))
        assertFalse(service.uploadUpdate(novel))
        novel.let {
            it.chaptersCount = 13
            it.updateTime = null
        }
        assertTrue(service.uploadUpdate(novel))
    }

}