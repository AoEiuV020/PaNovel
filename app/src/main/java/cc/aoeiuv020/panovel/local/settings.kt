package cc.aoeiuv020.panovel.local

/**
 * 设置，
 * Created by AoEiuV020 on 2017.10.04-14:04:44.
 */
object Settings : LocalSource {
    var textSize: Int by PrimitiveDelegate(18)
    var lineSpacing: Int by PrimitiveDelegate(2)
    var paragraphSpacing: Int by PrimitiveDelegate(4)
    var backgroundColor: Int by PrimitiveDelegate(0xffffffff.toInt())
    var textColor: Int by PrimitiveDelegate(0xff000000.toInt())

    var historyCount: Int by PrimitiveDelegate(30)

    var asyncThreadCount: Int by PrimitiveDelegate(30)
}

