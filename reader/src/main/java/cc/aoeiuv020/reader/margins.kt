package cc.aoeiuv020.reader

import cc.aoeiuv020.pager.IMarginsImpl

/**
 * 阅读器页面中有显示时间，电量之类的，
 * 每一个都可以设置留白，可以禁用，
 * 为了隐藏pager模块的接口IMargins而声明一个自己的接口，
 * 不继承IMargins主要是，
 * app依赖reader, reader依赖pager,
 * app使用reader中继承pager接口的接口也会编译出错，stub截断就过不去，
 *
 * Created by AoEiuV020 on 2018.06.17-16:07:30.
 */
interface ItemMargins {
    /**
     * 对应的东西是否显示，
     */
    val enabled: Boolean
    val left: Int
    val top: Int
    val right: Int
    val bottom: Int
}

fun ItemMargins.toIMargins() = IMarginsImpl(
        left = this.left,
        top = this.top,
        right = this.right,
        bottom = this.bottom
)