package cc.aoeiuv020.panovel.server

/**
 *
 * Created by AoEiuV020 on 2018.04.06-13:03:41.
 */
class ServerInfo {
    companion object {
        const val SERVER_INFO_ON_GITHUB = "https://raw.githubusercontent.com/AoEiuV020/PaNovel/static/static/serverInfo.json"
    }

    var minVersion: String = "0"
    var baseSite: String = "panovel.aoeiuv020.cc"
    var updateWebSocketAddress: String = "ws://$baseSite/ws/update"
    var queryIdsAddress: String = "http://$baseSite/bookshelf/queryIds"
    fun setAllAddress(site: String) {
        baseSite = site
        updateWebSocketAddress = "ws://$site/ws/update"
        queryIdsAddress = "http://$site/bookshelf/queryIds"
    }
}
