package cc.aoeiuv020.panovel.server.service.impl

import cc.aoeiuv020.panovel.server.ServerAddress
import cc.aoeiuv020.panovel.server.common.info
import cc.aoeiuv020.panovel.server.common.toBean
import cc.aoeiuv020.panovel.server.common.toJson
import cc.aoeiuv020.panovel.server.dal.model.MobRequest
import cc.aoeiuv020.panovel.server.dal.model.MobResponse
import cc.aoeiuv020.panovel.server.dal.model.autogen.Novel
import cc.aoeiuv020.panovel.server.service.NovelService
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

/**
 *
 * Created by AoEiuV020 on 2018.04.05-10:07:57.
 */
class NovelServiceImpl(private val serverAddress: ServerAddress) : NovelService {
    private val logger: Logger = LoggerFactory.getLogger(NovelServiceImpl::class.java.simpleName)
    override fun uploadUpdate(novel: Novel): Boolean {
        logger.info { "uploadUpdate ${novel.requesterExtra}: ${novel.updateTime}" }
        val mobRequest = MobRequest(novel.toJson())
        val response: MobResponse = Jsoup.connect(serverAddress.updateUploadUrl)
                .timeout(TimeUnit.SECONDS.toMillis(10).toInt())
                .header("Content-type", "application/json")
                .ignoreContentType(true)
                .requestBody(mobRequest.toJson())
                .method(Connection.Method.POST)
                .execute()
                .body()
                .toBean()
        return response.isSuccess() && response.getRealData()
    }

    override fun toString(): String {
        return serverAddress.updateUploadUrl
    }
}