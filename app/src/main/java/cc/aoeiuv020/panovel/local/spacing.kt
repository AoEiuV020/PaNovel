package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.pager.IMargins

/**
 * 排版相关的，上下左右留白的，
 * 单位都是百分比，
 * Created by AoEiuV020 on 2018.03.11-00:45:18.
 */
abstract class Margins : BaseLocalSource(), IMargins {
    override val path: String = "Settings/${this.javaClass.simpleName}"
    override var left: Int by PrimitiveDelegate(0)
    override var top: Int by PrimitiveDelegate(0)
    override var right: Int by PrimitiveDelegate(0)
    override var bottom: Int by PrimitiveDelegate(0)
}

/**
 * 小说内容的留白，
 */
class ContentMargins : Margins()