package cc.aoeiuv020.panovel.export

import android.content.Context
import cc.aoeiuv020.panovel.export.impl.ExporterV1
import net.lingala.zip4j.core.ZipFile
import net.lingala.zip4j.exception.ZipException
import net.lingala.zip4j.model.ZipParameters
import java.io.InputStream
import java.io.OutputStream

/**
 * Created by AoEiuV020 on 2018.05.11-17:45:49.
 */
class ExportManager(
        private val ctx: Context
) {
    companion object {
        const val NAME_TEMP = "PaNovel-Backup-00.zip"
        const val NAME_VERSION = "version"
        const val CURRENT_VERSION = 2
    }

    private fun getTempFile() =
            ctx.cacheDir.resolve(NAME_TEMP)
                    .apply {
                        exists() && delete()
                    }

    private fun getExporter(version: Int): IExporter = when (version) {
        1 -> ExporterV1(ctx)
        else -> throw IllegalStateException("该版本不存在<$version>,")
    }

    /**
     * 临时文件是固定的一个，单线程确保这个临时文件访问没问题，
     *
     * @param input 在外面close,
     */
    @Synchronized
    fun import(input: InputStream, options: Set<ExportOption>): String {
        val tempFile = getTempFile()
        // 先把input复制到临时文件再操作zip,
        tempFile.outputStream().use { output ->
            input.copyTo(output)
            output.flush()
        }

        val zipFile = ZipFile(tempFile)
        val version = try {
            zipFile.getFileHeader(NAME_VERSION).let {
                zipFile.getInputStream(it).use {
                    it.reader().readText()
                            .toInt()
                }
            }
        } catch (e: ZipException) {
            throw IllegalStateException("zip文件损坏", e)
        }

        // 根据不同版本选择不同的Exporter,
        val exporter = getExporter(version)
        return exporter.import(zipFile, options)
    }

    /**
     * 临时文件是固定的一个，单线程确保这个临时文件访问没问题，
     *
     * @param output 在外面close,
     */
    @Synchronized
    fun export(output: OutputStream, options: Set<ExportOption>): String {
        val tempFile = getTempFile()

        val zipFile = ZipFile(tempFile)
        try {
            zipFile.addStream("$CURRENT_VERSION".byteInputStream(), ZipParameters().apply {
                isSourceExternalStream = true
                fileNameInZip = NAME_VERSION
            })
        } catch (e: ZipException) {
            throw IllegalStateException("创建zip文件失败，", e)
        }
        val exporter = getExporter(CURRENT_VERSION)
        val result = exporter.export(zipFile, options)

        // 先导出到临时文件再复制到output,
        tempFile.inputStream().use { input ->
            input.copyTo(output)
            output.flush()
        }

        return result
    }
}