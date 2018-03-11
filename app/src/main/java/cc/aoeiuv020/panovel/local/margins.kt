package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.pager.IMargins

/**
 * 排版相关的，上下左右留白的，
 * 单位都是百分比，
 * Created by AoEiuV020 on 2018.03.11-00:45:18.
 */
class Margins(name: String) : BaseLocalSource(), IMargins {
    override val path: String = "Settings/$name"
    /**
     * 对应的东西是否显示，
     * 除了小说内容，其他都支持不显示，
     */
    var enabled: Boolean by PrimitiveDelegate(true)
    override var left: Int by PrimitiveDelegate(0)
    override var top: Int by PrimitiveDelegate(0)
    override var right: Int by PrimitiveDelegate(0)
    override var bottom: Int by PrimitiveDelegate(0)
    override fun toString(): String {
        return "Margins($left, $top, $right, $bottom)"
    }
}
