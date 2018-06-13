package cc.aoeiuv020.irondb

import java.io.File

/**
 * Created by AoEiuV020 on 2018.06.13-15:55:42.
 */
interface FileWrapper {
    fun <T> use(block: (File) -> T): T
    fun delete(): Boolean
}