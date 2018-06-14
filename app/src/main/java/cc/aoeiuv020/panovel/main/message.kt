package cc.aoeiuv020.panovel.main

import android.content.Context
import cc.aoeiuv020.base.jar.get
import cc.aoeiuv020.base.jar.string
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.server.common.toBean
import cc.aoeiuv020.panovel.util.Delegates
import cc.aoeiuv020.panovel.util.Pref
import cc.aoeiuv020.panovel.util.safelyShow
import com.google.gson.JsonObject
import org.jetbrains.anko.*

/**
 * 来自开发者的消息，
 * Created by AoEiuV020 on 2018.04.07-02:55:09.
 */
object DevMessage : Pref, AnkoLogger {
    override val name: String
        get() = "DevMessage"
    private var cachedMessage: String by Delegates.string("")
    private const val MESSAGE_URL = "https://raw.githubusercontent.com/AoEiuV020/PaNovel/static/static/message.json"
    fun asyncShowMessage(ctx: Context) {
        ctx.doAsync({ e ->
            val message = "获取来自开发者的消息失败，"
            Reporter.post(message, e)
            error(message, e)
        }) {
            val jsonObject: JsonObject = get(MESSAGE_URL).string().toBean()
            val message = jsonObject.get("message")?.asString
            if (message == null || message.isBlank() || message == cachedMessage) {
                return@doAsync
            }
            cachedMessage = message
            uiThread {
                ctx.alert {
                    title = jsonObject.get("title")?.asString ?: "来自开发者的消息"
                    this.message = message
                    yesButton { }
                }.safelyShow()
            }
        }
    }

}