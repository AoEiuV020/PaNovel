package cc.aoeiuv020.pager

/**
 *
 * Created by AoEiuV020 on 2018.03.11-01:57:32.
 */
interface IMargins {
    /**
     * 对应的东西是否显示，
     */
    var enabled: Boolean
    var left: Int
    var top: Int
    var right: Int
    var bottom: Int
}

class Margins(
        override var left: Int = 0,
        override var top: Int = 0,
        override var right: Int = 0,
        override var bottom: Int = 0,
        override var enabled: Boolean = true
) : IMargins
