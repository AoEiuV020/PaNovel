package cc.aoeiuv020.pager

/**
 *
 * Created by AoEiuV020 on 2018.03.11-01:57:32.
 */
interface IMargins {
    val left: Int
    val top: Int
    val right: Int
    val bottom: Int
}

class IMarginsImpl(
        override var left: Int = 0,
        override var top: Int = 0,
        override var right: Int = 0,
        override var bottom: Int = 0
) : IMargins {
    @Suppress("unused")
    constructor(margins: IMargins) : this(
            left = margins.left,
            top = margins.top,
            right = margins.right,
            bottom = margins.bottom
    )
}
