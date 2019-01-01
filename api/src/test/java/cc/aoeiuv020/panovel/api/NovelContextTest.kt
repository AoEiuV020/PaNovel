package cc.aoeiuv020.panovel.api

import org.junit.Test

/**
 * Created by AoEiuV020 on 2018.12.31-20:22:56.
 */
class NovelContextTest {
    @Test
    fun count() {
        println(NovelContext.getAllSite().count { !it.hide })
    }
}