package cc.aoeiuv020.panovel.server

/**
 *
 * Created by AoEiuV020 on 2018.04.06-13:03:41.
 */
class ServerAddress(
        private val host: String
) {
    companion object {
        private const val PANOVEL_HOST = "panovel.aoeiuv020.cc"

        fun getAndroidTest(): ServerAddress = new("panoveltest.aoeiuv020.cc")
        fun getDefault(): ServerAddress = new(PANOVEL_HOST)

        fun new(host: String): ServerAddress {
            return ServerAddress(host)
        }
    }

    val updateUploadUrl: String
        get() = "http://$host/novel/update"

    val needRefreshNovelListUrl: String
        get() = "http://$host/novel/needRefreshNovelList"

    val queryListUrl: String
        get() = "http://$host/novel/queryList"

    val touchUrl: String
        get() = "http://$host/novel/touch"

    val minVersionUrl: String
        get() = "http://$host/novel/minVersion"
}
