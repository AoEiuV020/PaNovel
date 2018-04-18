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
        const val PANOVEL_HOST = "panovel.aoeiuv020.cc"
        fun getOnline(): ServerAddress = Jsoup.connect(ServerAddress.SERVER_INFO_ON_GITHUB)
                .timeout(TimeUnit.SECONDS.toMillis(10).toInt())
                .execute()
                .body()
                .toBean()
    }

    val updateUploadUrl: String
        get() = data["updateUploadUrl"]
                ?: "http://$PANOVEL_HOST/update/upload"
}
