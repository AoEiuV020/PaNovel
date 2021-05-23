package cc.aoeiuv020.panovel.util

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cc.aoeiuv020.panovel.ad.AdConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DnsUtilsAndroidTest {

    @Before
    fun init() {
        DnsUtils.init(ApplicationProvider.getApplicationContext())
    }

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