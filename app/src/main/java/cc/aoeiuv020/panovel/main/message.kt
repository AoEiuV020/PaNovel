package cc.aoeiuv020.panovel.main

import android.content.Context
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.util.Delegates
import cc.aoeiuv020.panovel.util.Pref
import cc.aoeiuv020.panovel.util.safelyShow
import org.jetbrains.anko.*

/**
 * 来自开发者的消息，
 * Created by AoEiuV020 on 2018.04.07-02:55:09.
 */
object DevMessage : Pref, AnkoLogger {
    override val name: String
        get() = "DevMessage"
    private var cachedMessage: String by Delegates.string("")
    fun asyncShowMessage(ctx: Context) {
        ctx.doAsync({ e ->
            val message = "获取来自开发者的消息失败，"
            Reporter.post(message, e)
            error(message, e)
        }) {
            val message = DataManager.server.getMessage() ?: return@doAsync
            val messageTitle = message.title
            val messageContent = message.content
            if (messageContent == null || messageContent.isBlank() || messageContent == cachedMessage) {
                return@doAsync
            }
            cachedMessage = messageContent
            uiThread {
                ctx.alert {
                    title = messageTitle ?: "来自开发者的消息"
                    this.message = messageContent
                    yesButton { }
                }.safelyShow()
            }
        }
    }

}