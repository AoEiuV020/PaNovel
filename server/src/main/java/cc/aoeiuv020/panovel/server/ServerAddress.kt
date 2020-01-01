package cc.aoeiuv020.panovel.server

/**
 *
 * Created by AoEiuV020 on 2018.04.06-13:03:41.
 */
class ServerAddress(
        val host: String
) {
    companion object {
        private const val PANOVEL_HOST = "http://panovel.aoeiuv020.com"

        fun getAndroidTest(): ServerAddress = new("http://panoveltest.aoeiuv020.cc")
        fun getDefault(): ServerAddress = new(PANOVEL_HOST)

        fun new(host: String): ServerAddress {
            return ServerAddress(host)
        }
    }

    val updateUploadUrl: String
        get() = "$host/novel/update"

    val needRefreshNovelListUrl: String
        get() = "$host/novel/needRefreshNovelList"

    val queryListUrl: String
        get() = "$host/novel/queryList"

    val touchUrl: String
        get() = "$host/novel/touch"

    val minVersionUrl: String
        get() = "$host/novel/minVersion"

    val config: String
        get() = "$host/novel/config"

    val message: String
        get() = "$host/novel/message"
}
