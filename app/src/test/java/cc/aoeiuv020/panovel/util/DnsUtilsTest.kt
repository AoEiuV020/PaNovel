package cc.aoeiuv020.panovel.util

import cc.aoeiuv020.panovel.BuildConfig
import cc.aoeiuv020.panovel.ad.AdConstants
import cc.aoeiuv020.panovel.server.ServerAddress
import cc.aoeiuv020.panovel.server.dal.model.Config
import cc.aoeiuv020.panovel.server.dal.model.Message
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URLEncoder

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

    @Test
    fun testBean() {
        val config: Config = DnsUtils.txtToBean(ServerAddress.CONFIG_HOST)
        println(config)
        assertTrue(VersionName(config.minVersion) <= VersionName(BuildConfig.VERSION_NAME))
    }

    @Test
    fun makeMessage() {
        val msg = Message(
            "测试通知",
            "test"
        )
        println("title=${urle(msg.title)}&content=${urle(msg.content)}")
    }

    private fun urle(s: String?) =
        URLEncoder.encode(s, "utf8")
}