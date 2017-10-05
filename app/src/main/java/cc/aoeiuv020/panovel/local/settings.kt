package cc.aoeiuv020.panovel.local

/**
 * 设置，
 * Created by AoEiuV020 on 2017.10.04-14:04:44.
 */
object Settings : LocalSource {
    var textSize: Int by ContextDelegate(18)
    var lineSpacing: Int by ContextDelegate(2)
    var paragraphSpacing: Int by ContextDelegate(4)
    var backgroundColor: Int by ContextDelegate(0xffffffff.toInt())
    var textColor: Int by ContextDelegate(0xff000000.toInt())
}

