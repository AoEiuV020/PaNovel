package cc.aoeiuv020.panovel.settings

import cc.aoeiuv020.panovel.util.Delegates
import cc.aoeiuv020.panovel.util.Pref

/**
 * Created by AoEiuV020 on 2018.05.25-23:07:17.
 */
object GeneralSettings : Pref {
    override val name: String
        get() = "General"
    /**
     * 搜索线程数，
     */
    var searchThreadsLimit: Int by Delegates.int(4)
    var historyCount: Int by Delegates.int(30)
}
