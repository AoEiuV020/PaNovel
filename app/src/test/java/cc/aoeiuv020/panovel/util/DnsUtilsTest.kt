package cc.aoeiuv020.panovel.util

import cc.aoeiuv020.panovel.ad.AdConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Created by AoEiuV020 on 2021.05.15-18:30:12.
 */
class DnsUtilsTest {

    @Test
    fun test13lm() {
        val txtList = DnsUtils.getTxtList(AdConstants.HOST_13LM)
        println(txtList)
        assertTrue(txtList.contains("enabled=1"))
    }

    @Test
    fun testParseTxt() {
        val txtMap = DnsUtils.parseTxt(AdConstants.HOST_13LM)
        println(txtMap)
        assertEquals("1", txtMap["enabled"])
    }
}