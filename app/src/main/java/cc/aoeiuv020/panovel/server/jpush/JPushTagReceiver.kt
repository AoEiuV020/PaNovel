package cc.aoeiuv020.panovel.server.jpush

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager
import cn.jpush.android.api.JPushMessage
import com.google.gson.GsonBuilder
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.error

/**
 * Created by AoEiuV020 on 2018.04.17-12:17:54.
 */
class JPushTagReceiver(
        private val callback: (JPushMessage, TagAliasBean) -> Unit
) : BroadcastReceiver(), AnkoLogger {
    companion object {
        private const val EXTRA_TAG_ALIAS_BEAN = "tagAliasBean"
        private const val EXTRA_MESSAGE = "jPushMessage"
        private const val ACTION_TAG = "actionTag"
        private val gson = GsonBuilder().create()
        fun send(context: Context, jPushMessage: JPushMessage, tagAliasBean: TagAliasBean) {
            val intent = Intent(ACTION_TAG).apply {
                val tagAliasBeanString = gson.toJson(tagAliasBean)
                val jPushMessageString = gson.toJson(jPushMessage)
                putExtra(EXTRA_TAG_ALIAS_BEAN, tagAliasBeanString)
                putExtra(EXTRA_MESSAGE, jPushMessageString)
            }
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }

        fun register(context: Context, receiver: JPushTagReceiver) {
            val filter = IntentFilter().apply {
                addAction(ACTION_TAG)
            }
            LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter)
        }

        fun unregister(context: Context, receiver: JPushTagReceiver) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
        }

        fun create(callback: (JPushMessage, TagAliasBean) -> Unit): JPushTagReceiver {
            return JPushTagReceiver(callback)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val tagAliasBeanString = intent.getStringExtra(EXTRA_TAG_ALIAS_BEAN) ?: return
        val messageString = intent.getStringExtra(EXTRA_MESSAGE) ?: return
        try {
            val tagAliasBean = gson.fromJson(tagAliasBeanString, TagAliasBean::class.java)
            val jPushMessage = gson.fromJson(messageString, JPushMessage::class.java)
            debug { "receive: $tagAliasBean" }
            debug { "message: $jPushMessage" }
            callback(jPushMessage, tagAliasBean)
        } catch (e: Exception) {
            error("解析失败，", e)
            return
        }
    }
}