package cc.aoeiuv020.panovel.server

import cc.aoeiuv020.panovel.server.common.toBean
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit

/**
 *
 * Created by AoEiuV020 on 2018.04.06-13:03:41.
 */
class ServerAddress(
        var minVersion: String = "0",
        var data: Map<String, String> = mapOf()
) {
    companion object {
        private const val SERVER_INFO_ON_GITHUB = "https://raw.githubusercontent.com/AoEiuV020/PaNovel/static/static/serverInfo.json"
        private const val CONFIG_TEMPLATE = """
{
    "data": {
        "updateUploadUrl": "http://#host#/update/upload",
        "needRefreshNovelListUrl": "http://#host#/novel/needRefreshNovelList",
        "queryUrl": "http://#host#/novel/query",
        "touchUrl": "http://#host#/novel/touch"
    }
}
            """
        const val PANOVEL_HOST = "panovel.aoeiuv020.cc"

        fun getAndroidTest(): ServerAddress = new("192.168.1.10:8080")

        fun new(host: String): ServerAddress {
            return CONFIG_TEMPLATE
                    .replace("#host#", host)
                    .toBean()
        }

        fun getOnline(): ServerAddress = Jsoup.connect(ServerAddress.SERVER_INFO_ON_GITHUB)
                .timeout(TimeUnit.SECONDS.toMillis(10).toInt())
                .execute()
                .body()
                .toBean()
    }

    val updateUploadUrl: String
        get() = data["updateUploadUrl"]
                ?: "http://$PANOVEL_HOST/novel/update"

    val needRefreshNovelListUrl: String
        get() = data["needRefreshNovelListUrl"]
                ?: "http://$PANOVEL_HOST/novel/needRefreshNovelList"

    val queryUrl: String
        get() = data["queryUrl"]
                ?: "http://$PANOVEL_HOST/novel/query"

    val touchUrl: String
        get() = data["touchUrl"]
                ?: "http://$PANOVEL_HOST/novel/touch"
}
