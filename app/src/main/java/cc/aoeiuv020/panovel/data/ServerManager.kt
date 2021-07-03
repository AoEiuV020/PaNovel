package cc.aoeiuv020.panovel.data

import android.content.Context
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.server.ServerManager
import cc.aoeiuv020.panovel.server.dal.model.Message
import cc.aoeiuv020.panovel.server.dal.model.QueryResponse
import cc.aoeiuv020.panovel.server.toServer

/**
 *
 * Created by AoEiuV020 on 2018.05.31-18:22:55.
 */
class ServerManager(@Suppress("UNUSED_PARAMETER") ctx: Context) {

    /**
     * 订阅书架列表，覆盖所有tag,
     */
    fun setTags(list: List<Novel>) {
    }

    /**
     * 添加这些小说的订阅，
     */
    fun addTags(
        list: List<Novel>, callback: (List<Novel>) -> Unit = { novelList ->
        }
    ) {
    }

    fun removeTags(
        list: List<Novel>, callback: (List<Novel>) -> Unit = { novelList ->
        }
    ) {
    }

    fun touchUpdate(novel: Novel) = ServerManager.touch(novel.toServer())
    fun askUpdate(list: List<Novel>): Map<Long, QueryResponse> =
        ServerManager.queryList(list.map { it.nId to it.toServer() }.toMap())

    fun getMessage(): Message? = ServerManager.message()
}