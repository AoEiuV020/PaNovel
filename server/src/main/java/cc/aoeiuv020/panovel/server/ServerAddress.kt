package cc.aoeiuv020.panovel.server

/**
 *
 * Created by AoEiuV020 on 2018.04.06-13:03:41.
 */
class ServerAddress(
    val baseUrl: String
) {
    companion object {
        const val MESSAGE_HOST = "msg.panovel.aoeiuv020.com"
        const val CONFIG_HOST = "panovel.aoeiuv020.com"
        private const val PANOVEL_API_HOST = "http://panovel.aoeiuv020.com"

        fun getDefault(): ServerAddress = new(PANOVEL_API_HOST)

        fun new(host: String): ServerAddress {
            return ServerAddress(host)
        }
    }

    val updateUploadUrl: String
        get() = "$baseUrl/novel/update"

    val needRefreshNovelListUrl: String
        get() = "$baseUrl/novel/needRefreshNovelList"

    val queryListUrl: String
        get() = "$baseUrl/novel/queryList"

    val touchUrl: String
        get() = "$baseUrl/novel/touch"

    val minVersionUrl: String
        get() = "$baseUrl/novel/minVersion"

    val config: String
        get() = "$baseUrl/novel/config"

    val message: String
        get() = "$baseUrl/novel/message"
}
