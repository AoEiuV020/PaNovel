package cc.aoeiuv020.panovel.settings

import cc.aoeiuv020.panovel.util.Delegates
import cc.aoeiuv020.panovel.util.Pref

/**
 * Created by AoEiuV020 on 2018.06.05-12:17:02.
 */
object ServerSettings : Pref {
    override val name: String
        get() = "Server"
    /**
     * 收到小说更新推送时是否弹出通知小说更新，
     */
    var notifyNovelUpdate: Boolean by Delegates.boolean(true)
    /**
     * 更新推送的通知只保留最后一个，
     */
    var singleNotification: Boolean by Delegates.boolean(true)
    /**
     * 是否询问服务器有无更新，
     * 并会刷新“上次刷新”为服务器上的时间，
     * 省流量用，
     */
    var askUpdate: Boolean by Delegates.boolean(true)
}
