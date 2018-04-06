package cc.aoeiuv020.panovel.server

/**
 *
 * Created by AoEiuV020 on 2018.04.06-13:03:41.
 */
class ServerInfo {
    var baseSite: String = "panovel.aoeiuv020.cc"
        set(value) {
            field = value
            updateWebSocketAddress = "ws://$value/ws/update"
            queryIdsAddress = "http://$value/bookshelf/queryIds"
        }
    var updateWebSocketAddress: String = "ws://$baseSite/ws/update"
    var queryIdsAddress: String = "http://$baseSite/bookshelf/queryIds"
}
