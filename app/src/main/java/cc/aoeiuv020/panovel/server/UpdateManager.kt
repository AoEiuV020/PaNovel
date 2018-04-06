package cc.aoeiuv020.panovel.server

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.server.common.toBean
import cc.aoeiuv020.panovel.server.common.toJson
import cc.aoeiuv020.panovel.server.dal.model.autogen.Novel
import cc.aoeiuv020.panovel.util.notify
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import java.util.*

/**
 *
 * Created by AoEiuV020 on 2018.04.06-02:37:52.
 */
object UpdateManager : BroadcastReceiver(), AnkoLogger {
    private val ACTION_UPDATE = UpdateManager::class.java.name + ".UPDATE"
    private lateinit var service: UpdateService
    fun uploadUpdate(context: Context, novelItem: NovelItem, chaptersCount: Int, date: Date?) {
        debug { "uploadUpdate send broadcast" }
        val intent = Intent(ACTION_UPDATE)
        intent.putExtra("novelItem", novelItem.toJson())
        intent.putExtra("chaptersCount", chaptersCount)
        date?.let {
            intent.putExtra("date", date.time)
        }
        LocalBroadcastManager.getInstance(context)
                .sendBroadcast(intent)
    }

    /**
     * 收到更新并下载到更新后调用本方法弹出通知，
     */
    fun downloadUpdate(context: Context, novel: Novel, novelItem: NovelItem, chapters: List<NovelChapter>) {
        val text = chapters.last().name
        val title = "《${novelItem.name}》有更新"
        val time = novel.updateTime?.time
        context.notify(20000 + novel.id, text, title, time = time)
    }

    fun registerReceiver(updateService: UpdateService) {
        debug { "registerReceiver ${updateService.javaClass}" }
        this.service = updateService
        val filter = IntentFilter().apply {
            addAction(ACTION_UPDATE)
        }
        LocalBroadcastManager.getInstance(updateService).registerReceiver(this, filter)
    }

    fun unregisterReceiver(context: Context) {
        debug { "unregisterReceiver ${context.javaClass}" }
        LocalBroadcastManager.getInstance(context).unregisterReceiver(this)
    }

    override fun onReceive(context: Context, intent: Intent) {
        debug { "onReceive ${intent.action}" }
        when (intent.action) {
            ACTION_UPDATE -> {
                debug { "uploadUpdate receive broadcast" }
                val novelItem = intent.getStringExtra("novelItem").toBean<NovelItem>()
                val chaptersCount = intent.getIntExtra("chaptersCount", 0)
                val dateTime = intent.getLongExtra("date", 0)
                val updateTime = Date(dateTime)
                service.uploadUpdate(novelItem, chaptersCount, updateTime)
            }
            else -> {
            }
        }
    }

}