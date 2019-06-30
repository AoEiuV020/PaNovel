package cc.aoeiuv020.panovel.backup.impl

import cc.aoeiuv020.panovel.backup.BackupOption
import cc.aoeiuv020.panovel.backup.IBackup
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.error
import java.io.File

/**
 * Created by AoEiuV020 on 2018.05.11-20:27:17.
 */
abstract class DefaultBackup : IBackup, AnkoLogger {

    /**
     * @return 返回选项对应的名字和文件名，
     */
    private fun getOptionName(option: BackupOption): String = when (option) {
        BackupOption.Bookshelf -> "书架"
        BackupOption.BookList -> "书单"
        BackupOption.Progress -> "进度"
        BackupOption.Settings -> "设置"
    }

    @Synchronized
    override fun import(base: File, options: Set<BackupOption>): String {
        debug {
            "import from $base\n enable $options"
        }
        val sb = StringBuilder()
        options.forEach { option ->
            val name = getOptionName(option)
            try {
                val count = import(base.resolve(option.name), option)
                sb.appendln("成功导入$name: <$count>条，")
            } catch (e: Exception) {
                // 其中一项出异常时继续其他项，
                error("读取[$name]失败，", e)
            }
        }
        return sb.toString()
    }

    abstract fun import(file: File, option: BackupOption): Int

    @Synchronized
    override fun export(base: File, options: Set<BackupOption>): String {
        debug {
            "export to $base\n enable $options"
        }
        val sb = StringBuilder()
        options.forEach { option ->
            val name = getOptionName(option)
            try {
                val count = export(base.resolve(option.name), option)
                sb.appendln("成功导出$name: <$count>条，")
            } catch (e: Exception) {
                // 其中一项出异常时继续其他项，
                error("写入[$name]失败，", e)
            }
        }
        return sb.toString()
    }

    // 只保留最新版的导出，
    open fun export(file: File, option: BackupOption): Int {
        throw UnsupportedOperationException()
    }

}