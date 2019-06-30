package cc.aoeiuv020.panovel.data

import android.content.Context
import cc.aoeiuv020.panovel.App
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.server.ServerManager
import cc.aoeiuv020.panovel.server.common.md5
import cc.aoeiuv020.panovel.server.dal.model.QueryResponse
import cc.aoeiuv020.panovel.server.jpush.ExampleUtil
import cc.aoeiuv020.panovel.server.jpush.JPushTagReceiver
import cc.aoeiuv020.panovel.server.jpush.TagAliasBean
import cc.aoeiuv020.panovel.server.jpush.TagAliasOperatorHelper
import cc.aoeiuv020.panovel.server.toServer
import cc.aoeiuv020.panovel.settings.ServerSettings
import org.jetbrains.anko.doAsync
import java.util.concurrent.atomic.AtomicInteger
import cc.aoeiuv020.panovel.server.dal.model.autogen.Novel as ServerNovel

/**
 * TODO: 极光tag相关只有异步，这里只处理一个一个请求的情况，并发操作无视错乱，
 *
 * Created by AoEiuV020 on 2018.05.31-18:22:55.
 */
class ServerManager(@Suppress("UNUSED_PARAMETER") ctx: Context) {
    private val sequence: AtomicInteger = AtomicInteger()

    init {
        // 初始化，其中有用到Handler，要在主线程初始化，
        TagAliasOperatorHelper.getInstance()
    }

    private val setTagsReceiver = JPushTagReceiver()
    /**
     * 订阅书架列表，覆盖所有tag,
     */
    fun setTags(list: List<Novel>) {
        // 只响应最后一次订阅的回调，
        setTagsReceiver.callback = { _, tagAliasBean ->
            // 传入的列表，计算有几本在当前tag列表中，理应刚好相等，
            val count = list.map { it.toServer().md5() }.sumBy {
                if (it in tagAliasBean.tags) 1 else 0
            }
            val message = "成功订阅当前书架<$count>本，"
            // 收到订阅结果就简单弹个通知，
            ExampleUtil.showToast(message, App.ctx)
            // 只用一次就取消，
            // 出错的话没有取消，不大好，
            JPushTagReceiver.unregister(App.ctx, setTagsReceiver)
        }
        JPushTagReceiver.register(App.ctx, setTagsReceiver)
        doAsync {
            val bean = TagAliasBean()
            bean.action = TagAliasOperatorHelper.ACTION_SET
            bean.tags = list.map {
                it.toServer().md5()
            }.toSet()
            TagAliasOperatorHelper.getInstance().handleAction(App.ctx, sequence.getAndIncrement(), bean)
        }
    }

    /**
     * 添加这些小说的订阅，
     */
    fun addTags(list: List<Novel>, callback: (List<Novel>) -> Unit = { novelList ->
        val message = "成功订阅小说<${novelList.singleOrNull()?.run { "$site.$author.$name" }
                ?: "${novelList.size}本"}>"
        // 收到订阅结果就简单弹个通知，
        if (ServerSettings.subscriptToast) {
            ExampleUtil.showToast(message, App.ctx)
        }
    }) {
        val addTagsReceiver = JPushTagReceiver()
        // 只响应最后一次订阅的回调，
        addTagsReceiver.callback = { _, _ ->
            callback(list)
            // 只用一次就取消，
            // 出错的话没有取消，不大好，
            JPushTagReceiver.unregister(App.ctx, addTagsReceiver)
        }
        JPushTagReceiver.register(App.ctx, addTagsReceiver)
        val bean = TagAliasBean()
        bean.action = TagAliasOperatorHelper.ACTION_ADD
        bean.tags = list.map {
            it.toServer().md5()
        }.toSet()
        TagAliasOperatorHelper.getInstance().handleAction(App.ctx, sequence.getAndIncrement(), bean)
    }

    fun removeTags(list: List<Novel>, callback: (List<Novel>) -> Unit = { novelList ->
        val message = "成功取消订阅小说<${novelList.singleOrNull()?.run { "$site.$author.$name" }
                ?: "${novelList.size}本"}>"
        // 收到订阅结果就简单弹个通知，
        if (ServerSettings.subscriptToast) {
            ExampleUtil.showToast(message, App.ctx)
        }
    }) {
        val removeTagsReceiver = JPushTagReceiver()
        // 只响应最后一次订阅的回调，
        removeTagsReceiver.callback = { _, _ ->
            callback(list)
            // 只用一次就取消，
            // 出错的话没有取消，不大好，无所谓了，
            JPushTagReceiver.unregister(App.ctx, removeTagsReceiver)
        }
        JPushTagReceiver.register(App.ctx, removeTagsReceiver)
        val bean = TagAliasBean()
        bean.action = TagAliasOperatorHelper.ACTION_DELETE
        bean.tags = list.map {
            it.toServer().md5()
        }.toSet()
        TagAliasOperatorHelper.getInstance().handleAction(App.ctx, sequence.getAndIncrement(), bean)
    }

    fun touchUpdate(novel: Novel) = ServerManager.touch(novel.toServer())
    fun askUpdate(list: List<Novel>): Map<Long, QueryResponse> =
            ServerManager.queryList(list.map { it.nId to it.toServer() }.toMap())
}