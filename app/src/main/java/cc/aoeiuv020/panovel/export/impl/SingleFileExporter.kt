package cc.aoeiuv020.panovel.export.impl

import android.content.Context
import cc.aoeiuv020.panovel.export.ExportOption
import net.lingala.zip4j.core.ZipFile
import net.lingala.zip4j.model.ZipParameters
import java.io.InputStream
import java.io.OutputStream

/**
 * Created by AoEiuV020 on 2018.05.30-16:19:08.
 */
abstract class SingleFileExporter(
        ctx: Context
) : DefaultExporter(ctx) {
    companion object {
        const val NAME_TEMP = "DefaultExporter.tmp"
        val zipParameters = ZipParameters().apply {
            isSourceExternalStream = true
        }
    }

    private fun getTempFile() =
            ctx.cacheDir.resolve(NAME_TEMP)
                    .apply {
                        exists() && delete()
                    }

    /**
     * @return 返回成功导入的条目数，
     */
    abstract fun import(input: InputStream, option: ExportOption): Int

    abstract fun export(output: OutputStream, option: ExportOption): Int

    private fun getFileName(option: ExportOption) = option.name
    override fun import(zipFile: ZipFile, option: ExportOption): Int {
        return zipFile.getInputStream(zipFile.getFileHeader(getFileName(option))).use { input ->
            import(input, option)
        }
    }

    override fun export(zipFile: ZipFile, option: ExportOption): Int {
        val tempFile = getTempFile()
        val count = tempFile.outputStream().use { output ->
            export(output, option).also {
                output.flush()
            }
        }
        zipParameters.fileNameInZip = getFileName(option)
        zipFile.addFile(tempFile, zipParameters)
        return count
    }
}