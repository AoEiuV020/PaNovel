package cc.aoeiuv020.panovel.export.impl

import android.content.Context
import cc.aoeiuv020.panovel.export.ExportOption
import cc.aoeiuv020.panovel.export.IExporter
import net.lingala.zip4j.core.ZipFile
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.error

/**
 * Created by AoEiuV020 on 2018.05.11-20:27:17.
 */
abstract class DefaultExporter(
        protected val ctx: Context
) : IExporter, AnkoLogger {

    /**
     * @return 返回选项对应的名字和文件名，
     */
    private fun getOptionName(option: ExportOption): String = when (option) {
        ExportOption.Bookshelf -> "书架"
        ExportOption.BookList -> "书单"
        ExportOption.Settings -> "设置"
    }

    @Synchronized
    override fun import(zipFile: ZipFile, options: Set<ExportOption>): String {
        debug {
            "import from ${zipFile.file}\n enable $options"
        }
        val sb = StringBuilder()
        options.forEach { option ->
            val name = getOptionName(option)
            try {
                val count = import(zipFile, option)
                sb.appendln("成功导入$name: <$count>条，")
            } catch (e: Exception) {
                error("读取[$name]失败，", e)
            }
        }
        return sb.toString()
    }

    abstract fun import(zipFile: ZipFile, option: ExportOption): Int

    @Synchronized
    override fun export(zipFile: ZipFile, options: Set<ExportOption>): String {
        debug {
            "export to ${zipFile.file}\n enable $options"
        }
        val sb = StringBuilder()
        options.forEach { option ->
            val name = getOptionName(option)
            try {
                val count = export(zipFile, option)
                sb.appendln("成功导出$name: <$count>条，")
            } catch (e: Exception) {
                error("写入[$name]失败，", e)
            }
        }
        return sb.toString()
    }

    abstract fun export(zipFile: ZipFile, option: ExportOption): Int
}