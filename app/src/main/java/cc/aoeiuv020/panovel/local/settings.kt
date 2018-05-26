package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.panovel.App
import org.jetbrains.anko.AnkoLogger

/**
 *
 * Created by AoEiuV020 on 2017.10.04-14:04:44.
 */

/**
 * 私有设置，保存在应用私有目录下，
 */
object PrimarySettings : BaseLocalSource(), AnkoLogger {
    var baseFilePath: String = App.ctx.filesDir.path
}

/**
 * 所有设置，保存位置可以设置，
 */
object Settings : BaseLocalSource(), AnkoLogger {

    var asyncThreadCount: Int by PrimitiveDelegate(30)

}

