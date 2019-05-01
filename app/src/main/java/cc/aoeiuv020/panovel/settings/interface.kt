package cc.aoeiuv020.panovel.settings

import cc.aoeiuv020.panovel.util.Delegates
import cc.aoeiuv020.panovel.util.Pref

/**
 * Created by AoEiuV020 on 2019.05.01-13:23:43.
 */
object InterfaceSettings : Pref {
    override val name: String
        get() = "Interface"
    /**
     * 标签居中显示，
     */
    var tabGravityCenter: Boolean by Delegates.boolean(true)
}