package cc.aoeiuv020.panovel.server

import cc.aoeiuv020.panovel.api.DetailRequester
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.NovelContext
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.local.Bookshelf
import cc.aoeiuv020.panovel.local.BookshelfModifyListener
import cc.aoeiuv020.panovel.local.Cache
import cc.aoeiuv020.panovel.local.bookId
import cc.aoeiuv020.panovel.server.common.toBean
import cc.aoeiuv020.panovel.server.common.toJson
import cc.aoeiuv020.panovel.server.dal.model.MobRequest
import cc.aoeiuv020.panovel.server.dal.model.MobResponse
import cc.aoeiuv020.panovel.server.dal.model.autogen.Novel
import cc.aoeiuv020.panovel.util.asyncExecutor
import okhttp3.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.error
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.util.*
import java.util.concurrent.TimeUnit

/**
 *
 * Created by AoEiuV020 on 2018.04.06-10:08:52.
 */
class UpdateWebSocket(
        private val service: UpdateService
) : BookshelfModifyListener, AnkoLogger {
    private val queryIdsService = QueryIdsService()
    private val map: MutableMap<Int, NovelItem> = mutableMapOf()
    private val listener = Listener()
    private var serverInfo = ServerInfo()
    private var giveUp: Boolean = false
    private var retryCount: Int = 0
    private var isRunning = false

    /**
     * 外部后台调用，开始连接，
     * TODO: 一次连接两个了好像，???
     */
    fun start() {
        debug { "start" }
        beforeConnect()
    }

    fun stop() {
        debug { "stop" }
        webSocket?.close(1000, "stop")
    }

    override fun onBookshelfAdd(novelItem: NovelItem) {
        debug { "onBookshelfAdd ${novelItem.bookId}" }
        asyncExecutor.execute {
            val id = queryIdsService.queryIds(listOf(novelItem.requester)).first()
            if (Bookshelf.contains(novelItem)) {
                map[id] = novelItem
                uploadBookshelfAdd(listOf(id))
            }
        }
    }

    override fun onBookshelfRemove(novelItem: NovelItem) {
        debug { "onBookshelfRemove ${novelItem.bookId}" }
        asyncExecutor.execute {
            // 应该没必要优化，
            val id = map.entries.first {
                it.value.requester == novelItem.requester
            }.key
            if (!Bookshelf.contains(novelItem)) {
                map.remove(id)
                uploadBookshelfRemove(listOf(id))
            }
        }
    }

    /**
     * 开始连接前，准备建立连接，
     * 连接断开后回到这里，
     */
    fun beforeConnect() {
        debug { "beforeConnect" }
        retryCount++
        // 这个5,上下浮动1也无所谓了，
        if (retryCount > 5) {
            giveUp = true
        }
        if (giveUp) {
            service.stopSelf()
            return
        }
        asyncExecutor.execute {
            try {
                this@UpdateWebSocket.serverInfo = Jsoup.connect(ServerInfo.SERVER_INFO_ON_GITHUB)
                        .timeout(TimeUnit.SECONDS.toMillis(10).toInt())
                        .execute()
                        .body()
                        .toBean()
            } catch (e: Exception) {
                error("query server info,", e)
                // 从github拿配置失败就试试默认，
            }
            connecting()
        }
    }

    fun connecting() {
        val wsUrl = serverInfo.updateWebSocketAddress
        debug { "connecting $wsUrl" }
        val client = OkHttpClient()
        val request = try {
            Request.Builder().url(wsUrl).build()
        } catch (e: IllegalArgumentException) {
            error(e)
            giveUp = true
            beforeConnect()
            return
        }
        webSocket = client.newWebSocket(request, listener)
    }

    private var webSocket: WebSocket? = null
    fun connected() {
        debug { "connected" }
        map.clear()
        val novelItems = Bookshelf.list()
        Bookshelf.addListener(this)
        val ids = try {
            queryIdsService.queryIds(novelItems.map { it.requester })
        } catch (e: Exception) {
            failure("query ids failed", e)
            giveUp = true
            return
        }
        ids.zip(novelItems).toMap(map)
        uploadBookshelfAdd(ids)
        retryCount = 0
    }

    private fun uploadBookshelfRemove(ids: List<Int>) {
        webSocket?.send(RequestMessage(Action.BOOKSHELF_REMOVE, ids.toJson()).toJson())

    }

    private fun uploadBookshelfAdd(ids: List<Int>) {
        webSocket?.send(RequestMessage(Action.BOOKSHELF_ADD, ids.toJson()).toJson())
    }

    private fun downloadUpdate(novel: Novel) {
        val novelItem = map[novel.id] ?: run {
            uploadBookshelfRemove(listOf(novel.id))
            return
        }
        asyncExecutor.execute {
            val cachedChapters = Cache.chapters.get(novelItem)
            // 如果存在update时间字段就对比这个，否则对比长度，
            fun Pair<Date?, Int?>.newerThan(other: List<NovelChapter>): Boolean {
                return first?.let { thisUpdate ->
                    other.last().update?.let { otherUpdate ->
                        thisUpdate > otherUpdate
                    } ?: false
                } ?: (second ?: 0 > other.size)
            }
            if (cachedChapters != null
                    && novel.run { updateTime to chaptersCount }.newerThan(cachedChapters)) {
                val novelContext = NovelContext.getNovelContextByUrl(novelItem.requester.url)
                val detail = novelContext.getNovelDetail(novelItem.requester)
                val chapters = novelContext.getNovelChaptersAsc(detail.requester).also { Cache.chapters.put(novelItem, it) }
                if (chapters.run { last().update to size }.newerThan(cachedChapters)) {
                    UpdateManager.downloadUpdate(service, novel, novelItem, chapters)
                }
            }
        }
    }

    fun uploadUpdate(novelItem: NovelItem, chaptersCount: Int, updateTime: Date?) {
        val id = map.entries.firstOrNull {
            it.value.requester == novelItem.requester
        }?.key ?: return
        val novel = Novel().also {
            it.id = id
            it.requesterType = novelItem.requester.type
            it.requesterExtra = novelItem.requester.extra
            it.chaptersCount = chaptersCount
            it.updateTime = updateTime
        }

        webSocket?.send(RequestMessage(Action.UPDATE, novel.toJson()).toJson())
    }

    fun message(text: String) {
        try {
            val message: ResponseMessage = text.toBean()
            when (message.action) {
                Action.UPDATE -> downloadUpdate(message.getRealData())
                else -> {
                }
            }
        } catch (e: Exception) {
            // 这里不捕获的话整个websocket连接就断了，
            e.printStackTrace()
        }
    }

    fun closed() {
        debug { "closed" }
        webSocket = null
        Bookshelf.removeListener(this)
        beforeConnect()
    }

    fun failure(message: String = "failure", t: Throwable?) {
        error(message, t)
        closed()
    }

    inner class Listener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket?, response: Response?) {
            this@UpdateWebSocket.webSocket = webSocket
            connected()
        }

        override fun onMessage(webSocket: WebSocket?, text: String?) {
            text?.let { message(it) }
        }

        override fun onClosing(webSocket: WebSocket?, code: Int, reason: String?) {
            webSocket?.close(1000, "closing")
        }

        override fun onClosed(webSocket: WebSocket?, code: Int, reason: String?) {
            closed()
        }

        override fun onFailure(webSocket: WebSocket?, t: Throwable?, response: Response?) {
            failure("WebSocket failure", t)
        }
    }

    inner class QueryIdsService {
        fun queryIds(requesterList: List<DetailRequester>): List<Int> {
            val url = serverInfo.queryIdsAddress
            val novels = requesterList.map {
                Novel().apply {
                    requesterType = it.type
                    requesterExtra = it.extra
                }
            }
            val mobRequest = MobRequest(novels.toJson())
            val jsonString = Jsoup.connect(url)
                    .header("Content-Type", "application/json")
                    .requestBody(mobRequest.toJson())
                    .ignoreContentType(true)
                    .method(Connection.Method.POST)
                    .execute()
                    .body()
            val mobResponse = jsonString.toBean<MobResponse>()
            return mobResponse.data.toBean()
        }
    }

}