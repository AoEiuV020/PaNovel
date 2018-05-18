package cc.aoeiuv020.panovel.api.site

import org.junit.Before
import org.junit.Test

/**
 * Created by AoEiuV020 on 2018.05.18-16:51:57.
 */
class YssmTest {
    init {
        System.setProperty("org.slf4j.simpleLogger.log.Yssm", "trace")
    }

    private lateinit var context: Yssm
    @Before
    fun setUp() {
        context = Yssm()
    }

    @Test
    fun getNovelList() {
        context.getNovelList(context.searchNovelName("都市").requester).forEach {
            println(it.novel)
        }
    }
}