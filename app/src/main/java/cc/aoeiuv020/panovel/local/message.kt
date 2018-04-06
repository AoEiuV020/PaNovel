package cc.aoeiuv020.panovel.local

import android.content.Context
import cc.aoeiuv020.panovel.server.common.toBean
import cc.aoeiuv020.panovel.util.async
import cc.aoeiuv020.panovel.util.suffixThreadName
import com.google.gson.JsonObject
import io.reactivex.Observable
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.alert
import org.jetbrains.anko.error
import org.jetbrains.anko.yesButton
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit

/**
 * 来自开发者的消息，
 * Created by AoEiuV020 on 2018.04.07-02:55:09.
 */
object DevMessage : BaseLocalSource(), AnkoLogger {
    private var cachedMessage: String by PrimitiveDelegate("")
    private const val MESSAGE_URL = "https://raw.githubusercontent.com/AoEiuV020/PaNovel/static/static/message.json"
    fun asyncShowMessage(ctx: Context) {
        Observable.fromCallable {
            suffixThreadName("showMessage")
            val jsonObject: JsonObject = Jsoup.connect(MESSAGE_URL)
                    .timeout(TimeUnit.SECONDS.toMillis(10).toInt())
                    .execute()
                    .body()
                    .toBean()
            jsonObject
        }.async().subscribe({ jsonObject ->
            val message = jsonObject.get("message")?.asString
            if (message == null || message.isBlank() || message == cachedMessage) {
                return@subscribe
            }
            cachedMessage = message
            ctx.alert {
                title = jsonObject.get("title")?.asString ?: "来自开发者的消息"
                this.message = message
                yesButton { }
            }.show()
        }, { e ->
            val message = "获取消息失败"
            error(message, e)
        })
    }

}