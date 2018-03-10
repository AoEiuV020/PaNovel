package cc.aoeiuv020.panovel.local

/**
 * 本地提供数据的数要实现这个接口，
 * 实现这个接口的类名不会被混淆，
 * Created by AoEiuV020 on 2017.10.04-20:02:28.
 */
interface LocalSource {
    /**
     * 数据保存在这个路径，
     */
    val path: String
}

abstract class BaseLocalSource : LocalSource {
    override val path: String = this.javaClass.simpleName
}