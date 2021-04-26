package cc.aoeiuv020.panovel.backup

import android.app.Activity
import cc.aoeiuv020.panovel.util.Pref
import java.io.File

/**
 * Created by AoEiuV020 on 2021.04.25-12:38:46.
 */
interface BackupHelper : Pref {
    override val name: String
        get() = "Backup$type"
    val type: String
    fun ready(): Boolean
    fun configPreview(): String
    fun configActivity(): Class<out Activity>
    fun restore(tempFile: File)
    fun backup(tempFile: File)
}