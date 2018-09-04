package cc.aoeiuv020.panovel.backup

import java.io.File

/**
 * Created by AoEiuV020 on 2018.05.11-17:46:28.
 */
interface IBackup {
    fun import(base: File, options: Set<BackupOption>): String
    fun export(base: File, options: Set<BackupOption>): String
}