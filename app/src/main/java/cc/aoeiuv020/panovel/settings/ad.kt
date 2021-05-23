package cc.aoeiuv020.panovel.settings

import cc.aoeiuv020.panovel.util.Delegates
import cc.aoeiuv020.panovel.util.Pref

object AdSettings : Pref {
    override val name: String
        get() = "Ad"

    // 提供彩蛋，满足条件就关闭广告，要在代码中改这个值，所以可变，var,
    var adEnabled: Boolean by Delegates.boolean(true)

    // 口袋工厂, https://www.13lm.com/
    var middle13lmEnabled: Boolean by Delegates.boolean(true)
}
