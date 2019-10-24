package cc.aoeiuv020.panovel.server.service.impl

import cc.aoeiuv020.anull.notNull
import cc.aoeiuv020.gson.type
import cc.aoeiuv020.log.debug
import cc.aoeiuv020.okhttp.OkHttpUtils
import cc.aoeiuv020.panovel.server.ServerAddress
import cc.aoeiuv020.panovel.server.common.bookId
import cc.aoeiuv020.panovel.server.common.toBean
import cc.aoeiuv020.panovel.server.common.toJson
import cc.aoeiuv020.panovel.server.dal.model.Config
import cc.aoeiuv020.panovel.server.dal.model.MobRequest
import cc.aoeiuv020.panovel.server.dal.model.MobResponse
import cc.aoeiuv020.panovel.server.dal.model.QueryResponse
import cc.aoeiuv020.panovel.server.dal.model.autogen.Novel
import cc.aoeiuv020.panovel.server.service.NovelService
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

/**
 *
 * Created by AoEiuV020 on 2018.04.05-10:07:57.
 */
class NovelServiceImpl(private val serverAddress: ServerAddress) : NovelService {
    override val host get() = serverAddress.host
    private val logger: Logger = LoggerFactory.getLogger(NovelServiceImpl::class.java.simpleName)
    private val client: OkHttpClient = OkHttpUtils.client.newBuilder()
            // 超时设置短一些，连不上就放弃，不是很重要，
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(3, TimeUnit.SECONDS)
            .writeTimeout(3, TimeUnit.SECONDS)
            .build()

    private inline fun <reified T> post(url: String, any: Any): T =
            post(url, any, type<T>())

    private fun <T> post(url: String, any: Any, type: Type): T {
        val mobRequest = MobRequest(any.toJson())
        val requestBody = RequestBody.create(
                MediaType.parse("application/json"),
                mobRequest.toJson()
        )
        val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()
        val call = client.newCall(request)
        // .string()会关闭body,
        val response: MobResponse = call.execute().body().notNull().string().notNull()
                .also { logger.debug { "response: $it" } }
                .toBean()
        if (!response.isSuccess()) {
            // 只能说可能是上传的参数不对，
            throw IllegalStateException("请求失败: ${response.data}")
        }
        return response.getRealData(type)
    }

    override fun uploadUpdate(novel: Novel): Boolean {
        logger.debug { "uploadUpdate <${novel.run { "$site.$author.$name" }}>" }
        return post(serverAddress.updateUploadUrl, novel)
    }

    override fun needRefreshNovelList(count: Int): List<Novel> {
        logger.debug { "needRefreshNovelList count = $count" }
        return post(serverAddress.needRefreshNovelListUrl, count)
    }

    override fun queryList(novelMap: Map<Long, Novel>): Map<Long, QueryResponse> {
        logger.debug { "queryList ${novelMap.map { "${it.key}=<${it.value.run { "$site.$author.$name" }}>" }}" }
        return post(serverAddress.queryListUrl, novelMap)
    }

    override fun touch(novel: Novel): Boolean {
        logger.debug { "touch ${novel.bookId}" }
        return post(serverAddress.touchUrl, novel)
    }

    override fun minVersion(): String {
        logger.debug { "minVersion" }
        return post(serverAddress.minVersionUrl, Any())
    }

    override fun config(): Config {
        logger.debug { "config" }
        return post(serverAddress.config, Any())
    }
}