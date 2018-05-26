package cc.aoeiuv020.panovel.settings

import cc.aoeiuv020.pager.IMargins
import cc.aoeiuv020.panovel.util.Delegates
import cc.aoeiuv020.panovel.util.SubPref

/**
 * Created by AoEiuV020 on 2018.05.26-20:36:04.
 */

class Margins(override val name: String, enabled: Boolean,
              left: Int, top: Int, right: Int, bottom: Int
) : SubPref(ReaderSettings), IMargins {
    // 保存在App.ctx.packageName + "_ReaderSettings" + "_$name"
    /**
     * 对应的东西是否显示，
     * 除了小说内容，其他都支持不显示，
     */
    override var enabled: Boolean by Delegates.boolean(enabled)
    override var left: Int by Delegates.int(left)
    override var top: Int by Delegates.int(top)
    override var right: Int by Delegates.int(right)
    override var bottom: Int by Delegates.int(bottom)
    override fun toString(): String {
        return "Margins($left, $top, $right, $bottom)"
    }
}

