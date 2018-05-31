package cc.aoeiuv020.panovel.data

import android.content.Context
import cc.aoeiuv020.panovel.App
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.server.UpdateManager
import cc.aoeiuv020.panovel.server.common.md5
import cc.aoeiuv020.panovel.server.jpush.JPushTagReceiver
import cc.aoeiuv020.panovel.server.jpush.TagAliasBean
import cc.aoeiuv020.panovel.server.jpush.TagAliasOperatorHelper
import cc.aoeiuv020.panovel.server.toServer
import org.jetbrains.anko.doAsync
import java.util.concurrent.atomic.AtomicInteger
import cc.aoeiuv020.panovel.server.dal.model.autogen.Novel as ServerNovel

/**
 * Created by AoEiuV020 on 2018.05.31-18:22:55.
 */
class ServerManager(@Suppress("UNUSED_PARAMETER") ctx: Context) {
    private val sequence: AtomicInteger = AtomicInteger()

    init {
        // 初始化这个负责上传更新的，
        UpdateManager.create(ctx)
    }

    private val subscriptBookshelfReceiver = JPushTagReceiver()
    fun subscriptBookshelf(list: List<Novel>, callback: (Int) -> Unit) {
        // 只响应最后一次订阅的回调，
        subscriptBookshelfReceiver.callback = { _, tagAliasBean ->
            callback(tagAliasBean.tags.size)
            // 只用一次就取消，
            // 出错的话没有取消，不大好，
            JPushTagReceiver.unregister(App.ctx, subscriptBookshelfReceiver)
        }
        JPushTagReceiver.register(App.ctx, subscriptBookshelfReceiver)
        // 初始化，其中有用到Handler，要在主线程初始化，
        TagAliasOperatorHelper.getInstance()
        doAsync {
            val bean = TagAliasBean()
            bean.action = TagAliasOperatorHelper.ACTION_SET
            bean.tags = list.map {
                it.toServer().md5()
            }.toSet()
            TagAliasOperatorHelper.getInstance().handleAction(App.ctx, sequence.getAndIncrement(), bean)
        }

    }

    fun askUpdate(novel: Novel): ServerNovel? = UpdateManager.query(novel.toServer())
}