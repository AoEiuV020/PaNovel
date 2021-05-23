package cc.aoeiuv020.panovel.util

import android.content.Context
import cc.aoeiuv020.gson.toJson
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.server.common.toBean
import org.xbill.DNS.*
import org.xbill.DNS.config.AndroidResolverConfigProvider
import java.net.URLDecoder


/**
 * Created by AoEiuV020 on 2021.05.15-18:27:30.
 */
object DnsUtils {
    fun init(ctx: Context) {
        AndroidResolverConfigProvider.setContext(ctx.applicationContext)
    }

    /**
     * 获取域名txt记录，失败直接返回空，
     */
    fun getTxtList(host: String): List<String> {
        val lookup = Lookup(host, Type.TXT)
        lookup.setResolver(SimpleResolver())
        lookup.setCache(null)
        val records: Array<Record>? = lookup.run()
        if (lookup.result == Lookup.SUCCESSFUL) {
            return records?.flatMap { (it as? TXTRecord)?.strings ?: emptyList() } ?: emptyList()
        } else {
            Reporter.post("获取txt记录失败: " + lookup.errorString)
        }
        return emptyList()
    }

    /**
     * txt记录解析成键值对，
     */
    fun parseTxt(host: String): Map<String, String> {
        return getTxtList(host)
            .flatMap { query ->
                query.split('&').map { entry ->
                    entry.split('=').let {
                        URLDecoder.decode(it[0], Charsets.UTF_8.name()) to URLDecoder.decode(
                            it[1],
                            Charsets.UTF_8.name()
                        )
                    }
                }
            }.toMap()
    }

    /**
     * txt记录解析成对象，
     */
    inline fun <reified T : Any> txtToBean(host: String): T {
        return parseTxt(host).toJson().toBean()
    }

}