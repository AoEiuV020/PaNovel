package cc.aoeiuv020.panovel.server.service.impl

import cc.aoeiuv020.base.jar.debug
import cc.aoeiuv020.panovel.server.ServerAddress
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
import java.lang.IllegalStateException
import java.util.concurrent.TimeUnit

/**
 *
 * Created by AoEiuV020 on 2018.04.05-10:07:57.
 */
class NovelServiceImpl(private val serverAddress: ServerAddress) : NovelService {
    private val logger: Logger = LoggerFactory.getLogger(NovelServiceImpl::class.java.simpleName)

    private inline fun <reified T> post(url: String, any: Any): T {
        val mobRequest = MobRequest(any.toJson())
        val response: MobResponse = Jsoup.connect(url)
                .timeout(TimeUnit.SECONDS.toMillis(10).toInt())
                .header("Content-type", "application/json")
                .ignoreContentType(true)
                .requestBody(mobRequest.toJson())
                .method(Connection.Method.POST)
                .execute()
                .body()
                .toBean()
        if (!response.isSuccess()) {
            // 只能说可能是上传的参数不对，
            throw IllegalStateException("请求失败，")
        }
        return response.getRealData()
    }

    override fun uploadUpdate(novel: Novel): Boolean {
        logger.debug { "uploadUpdate <${novel.run { "$site.$author.$name" }}>" }
        return post(serverAddress.updateUploadUrl, novel)
    }

    override fun needRefreshNovelList(count: Int): List<Novel> {
        logger.debug { "needRefreshNovelList count = $count" }
        return post(serverAddress.needRefreshNovelListUrl, count)
    }

    override fun query(novel: Novel): Novel {
        logger.debug { "query $novel" }
        return post(serverAddress.queryUrl, novel)
    }

    override fun touch(novel: Novel): Boolean {
        logger.debug { "touch $novel" }
        return post(serverAddress.touchUrl, novel)
    }
}