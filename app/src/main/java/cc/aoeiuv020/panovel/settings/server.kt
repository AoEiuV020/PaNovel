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
     * 是否通知小说更新，
     */
    var notifyNovelUpdate: Boolean by Delegates.boolean(true)
}
