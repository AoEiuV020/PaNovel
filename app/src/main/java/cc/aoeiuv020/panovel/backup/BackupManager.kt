package cc.aoeiuv020.panovel.backup

import android.content.Context
import cc.aoeiuv020.panovel.App
import cc.aoeiuv020.panovel.backup.impl.BackupV1
import cc.aoeiuv020.panovel.backup.impl.BackupV2
import cc.aoeiuv020.panovel.backup.impl.BackupV3
import net.lingala.zip4j.core.ZipFile
import net.lingala.zip4j.exception.ZipException
import net.lingala.zip4j.model.ZipParameters
import java.io.InputStream
import java.io.OutputStream


/**
 * Created by AoEiuV020 on 2018.05.11-17:45:49.
 */
class BackupManager {
    companion object {
        const val NAME_TEMP = "PaNovel-Backup-00.zip"
        const val FOLDER_TEMP = "PaNovel-Backup-00"
        const val NAME_VERSION = "version"
        const val CURRENT_VERSION = 3
    }

    private val ctx: Context = App.ctx

    private fun getTempFile() =
            ctx.cacheDir.resolve(NAME_TEMP)
                    .apply {
                        exists() && delete()
                    }

    private fun getTempFolder() =
            ctx.cacheDir.resolve(FOLDER_TEMP)
                    .apply {
                        exists() && deleteRecursively()
                    }.apply {
                        // 临时目录要存在，但是空内容，
                        mkdirs()
                    }

    private fun getExporter(version: Int): IBackup = when (version) {
        3 -> BackupV3()
        2 -> BackupV2()
        1 -> BackupV1()
        else -> throw IllegalStateException("版本<$version>不存在,")
    }

    /**
     * 临时文件是固定的一个，单线程确保这个临时文件访问没问题，
     *
     * @param input 在外面close,
     */
    @Synchronized
    fun import(input: InputStream, options: Set<BackupOption>): String {
        val folder = getTempFolder()
        try {
            val tempFile = getTempFile()
            // 先把input复制到临时文件再解压zip,
            tempFile.outputStream().use { output ->
                input.copyTo(output)
                output.flush()
            }
            val zipFile = ZipFile(tempFile)
            // 直接全部解压，
            zipFile.extractAll(folder.canonicalPath)
            // 用完删除临时文件，
            tempFile.delete()
        } catch (e: ZipException) {
            throw IllegalStateException("zip文件解压失败，", e)
        }
        val version = folder.resolve(NAME_VERSION).readText().toInt()
        // 根据不同版本选择不同的Exporter,
        val exporter = getExporter(version)

        val result = exporter.import(folder, options)
        // 用完删除临时文件夹，
        folder.deleteRecursively()
        return result
    }

    /**
     * 临时文件是固定的一个，单线程确保这个临时文件访问没问题，
     *
     * @param output 在外面close,
     */
    @Synchronized
    fun export(output: OutputStream, options: Set<BackupOption>): String {
        val folder = getTempFolder()
        folder.resolve(NAME_VERSION).writeText(CURRENT_VERSION.toString())
        val exporter = getExporter(CURRENT_VERSION)
        val result = exporter.export(folder, options)

        val tempFile = getTempFile()
        val zipFile = ZipFile(tempFile)
        zipFile.addFolder(folder, ZipParameters().apply {
            // folder里的文件直接挂到zip根目录，不单独一个文件夹，
            isIncludeRootFolder = false
        })

        folder.deleteRecursively()

        // 先导出到临时文件再复制到output,
        tempFile.inputStream().use { input ->
            input.copyTo(output)
            output.flush()
        }
        // 用完删除临时文件，
        tempFile.delete()

        return result
    }
}