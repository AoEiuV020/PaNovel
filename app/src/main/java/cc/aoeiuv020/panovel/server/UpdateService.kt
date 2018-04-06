package cc.aoeiuv020.panovel.server

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.local.bookId
import cc.aoeiuv020.panovel.util.asyncExecutor
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.startService
import java.util.*

/**
 *
 * Created by AoEiuV020 on 2018.04.06-02:41:34.
 */
class UpdateService : Service(), AnkoLogger {
    companion object {
        fun start(context: Context) {
            context.startService<UpdateService>()
        }
    }

    private val webSocket = UpdateWebSocket(this)
    private val updateManager = UpdateManager
    override fun onCreate() {
        super.onCreate()
        debug { "onCreate" }
        asyncExecutor.execute {
            webSocket.start()
        }
        updateManager.registerReceiver(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    fun uploadUpdate(novelItem: NovelItem, chaptersCount: Int, updateTime: Date?) {
        debug { "uploadUpdate ${novelItem.bookId}" }
        webSocket.uploadUpdate(novelItem, chaptersCount, updateTime)
    }

    override fun onDestroy() {
        debug { "onDestroy" }
        updateManager.unregisterReceiver(this)
        super.onDestroy()
    }
}