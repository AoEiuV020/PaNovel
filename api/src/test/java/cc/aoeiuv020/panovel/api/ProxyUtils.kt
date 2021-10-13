package cc.aoeiuv020.panovel.api

/**
 * Created by AoEiuV020 on 2021.10.13-23:39:51.
 */
object ProxyUtils {
    @Suppress("PrivatePropertyName")
    private val PROXY_ENABLED = false
    fun proxy() {
        if (PROXY_ENABLED) {
            System.setProperty("socksProxyHost", "localhost")
            System.setProperty("socksProxyPort", "1081")
        }
    }
}