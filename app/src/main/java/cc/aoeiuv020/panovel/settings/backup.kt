package cc.aoeiuv020.panovel.settings

import cc.aoeiuv020.panovel.util.Delegates
import cc.aoeiuv020.panovel.util.Pref

/**
 * Created by AoEiuV020 on 2021.04.25-13:42:29.
 */
object BackupSettings : Pref {
    override val name: String
        get() = "Backup"

    /**
     * 备份选中的单选框索引，最后一个其他就是-1,
     */
    var checkedButtonIndex: Int by Delegates.int(0)

}