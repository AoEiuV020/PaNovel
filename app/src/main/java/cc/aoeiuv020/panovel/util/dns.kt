package cc.aoeiuv020.panovel.util

import android.content.Context
import cc.aoeiuv020.panovel.report.Reporter
import org.xbill.DNS.*
import org.xbill.DNS.config.AndroidResolverConfigProvider


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
                        it[0] to it[1]
                    }
                }
            }.toMap()
    }
}