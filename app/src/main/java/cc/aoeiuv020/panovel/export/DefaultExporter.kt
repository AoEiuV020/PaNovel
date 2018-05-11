package cc.aoeiuv020.panovel.export

import android.content.Context
import net.lingala.zip4j.core.ZipFile
import net.lingala.zip4j.model.ZipParameters
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.error
import java.io.InputStream
import java.io.OutputStream

/**
 * Created by AoEiuV020 on 2018.05.11-20:27:17.
 */
abstract class DefaultExporter(
        private val ctx: Context
) : IExporter, AnkoLogger {
    companion object {
        const val NAME_TEMP = "DefaultExporter.tmp"
    }

    /**
     * @return 返回成功导入的条目数，
     */
    abstract fun import(input: InputStream, option: ExportOption): Int

    abstract fun export(output: OutputStream, option: ExportOption): Int

    private fun getTempFile() =
            ctx.cacheDir.resolve(NAME_TEMP)
                    .apply {
                        exists() && delete()
                    }

    /**
     * @return 返回选项对应的名字和文件名，
     */
    private fun getOptionPair(option: ExportOption): Pair<String, String> = when (option) {
        ExportOption.Bookshelf -> "书架"
        ExportOption.BookList -> "书单"
        ExportOption.Settings -> "设置"
    } to option.name

    @Synchronized
    override fun import(zipFile: ZipFile, options: Set<ExportOption>): String {
        debug {
            "import from ${zipFile.file}\n enable $options"
        }
        val sb = StringBuilder()
        options.forEach { option ->
            val (name, fileName) = getOptionPair(option)
            try {
                zipFile.getInputStream(zipFile.getFileHeader(fileName)).use { input ->
                    val count = import(input, option)
                    sb.appendln("成功导入$name: <$count>条，")
                }
            } catch (e: Exception) {
                error("读取[$name]失败，", e)
            }
        }
        return sb.toString()
    }

    @Synchronized
    override fun export(zipFile: ZipFile, options: Set<ExportOption>): String {
        debug {
            "export to ${zipFile.file}\n enable $options"
        }
        val sb = StringBuilder()
        val zipParameters = ZipParameters().apply {
            isSourceExternalStream = true
        }
        options.forEach { option ->
            val (name, fileName) = getOptionPair(option)
            try {
                val tempFile = getTempFile()
                val count = tempFile.outputStream().use { output ->
                    export(output, option).also {
                        output.flush()
                    }
                }
                zipParameters.fileNameInZip = fileName
                zipFile.addFile(tempFile, zipParameters)
                sb.appendln("成功导出$name: <$count>条，")
            } catch (e: Exception) {
                error("写入[$name]失败，", e)
            }
        }
        return sb.toString()
    }
}