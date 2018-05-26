package cc.aoeiuv020.panovel.settings

import cc.aoeiuv020.panovel.util.Pref
import cc.aoeiuv020.panovel.util.PrefDelegates

/**
 * Created by AoEiuV020 on 2018.05.25-23:07:17.
 */
object GeneralSettings : Pref {
    override val name: String
        get() = "General"
    val downloadThreadsLimit: Int by PrefDelegates.int(4)
    // 提供彩蛋，满足条件就关闭广告，要在代码中改这个值，所以可变，var,
    var adEnabled: Boolean by PrefDelegates.boolean(true)
}