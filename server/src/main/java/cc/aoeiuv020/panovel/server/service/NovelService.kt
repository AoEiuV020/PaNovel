package cc.aoeiuv020.panovel.server.service

import cc.aoeiuv020.panovel.server.dal.model.Config
import cc.aoeiuv020.panovel.server.dal.model.QueryResponse
import cc.aoeiuv020.panovel.server.dal.model.autogen.Novel

/**
 *
 * Created by AoEiuV020 on 2018.04.05-09:13:02.
 */
interface NovelService {
    fun uploadUpdate(novel: Novel): Boolean
    fun needRefreshNovelList(count: Int): List<Novel>
    fun queryList(novelMap: Map<Long, Novel>): Map<Long, QueryResponse>
    fun touch(novel: Novel): Boolean
    fun minVersion(): String
    fun config(): Config
}