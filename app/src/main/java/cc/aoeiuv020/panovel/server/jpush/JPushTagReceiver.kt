package cc.aoeiuv020.panovel.server.jpush

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager
import cc.aoeiuv020.base.jar.toBean
import cc.aoeiuv020.base.jar.toJson
import cc.aoeiuv020.panovel.report.Reporter
import cn.jpush.android.api.JPushMessage
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.error

/**
 * Created by AoEiuV020 on 2018.04.17-12:17:54.
 */
class JPushTagReceiver : BroadcastReceiver(), AnkoLogger {
    companion object {
        private const val EXTRA_TAG_ALIAS_BEAN = "tagAliasBean"
        private const val EXTRA_MESSAGE = "jPushMessage"
        private const val ACTION_TAG = "actionTag"
        // 收到极光tag消息时调用，通知所有注册了的receiver,
        fun send(context: Context, jPushMessage: JPushMessage, tagAliasBean: TagAliasBean) {
            val intent = Intent(ACTION_TAG).apply {
                val tagAliasBeanString = tagAliasBean.toJson()
                val jPushMessageString = jPushMessage.toJson()
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
    }

    lateinit var callback: (JPushMessage, TagAliasBean) -> Unit

    override fun onReceive(context: Context, intent: Intent) {
        val tagAliasBeanString = intent.getStringExtra(EXTRA_TAG_ALIAS_BEAN) ?: return
        val messageString = intent.getStringExtra(EXTRA_MESSAGE) ?: return
        try {
            val tagAliasBean = tagAliasBeanString.toBean<TagAliasBean>()
            val jPushMessage = messageString.toBean<JPushMessage>()
            debug { "receive: $tagAliasBean" }
            debug { "message: $jPushMessage" }
            if (::callback.isInitialized) {
                // tagAliasBean是自己生成并被TagAliasOperatorHelper缓存的，
                // jPushMessage是极光返回的当前情况，
                callback(jPushMessage, tagAliasBean)
            }
        } catch (e: Exception) {
            val message = "解析失败，"
            error(message, e)
            Reporter.post(message, e)
            return
        }
    }
}