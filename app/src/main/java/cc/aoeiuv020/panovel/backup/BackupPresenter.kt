package cc.aoeiuv020.panovel.backup

import android.net.Uri
import android.os.Environment
import cc.aoeiuv020.panovel.App
import cc.aoeiuv020.panovel.Presenter
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.backup.webdav.BackupWebDavHelper
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.settings.LocationSettings
import cc.aoeiuv020.panovel.util.notNullOrReport
import cc.aoeiuv020.regex.pick
import org.jetbrains.anko.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FilenameFilter
import java.io.IOException
import java.util.*

/**
 * Created by AoEiuV020 on 2018.05.11-12:39:10.
 */
class BackupPresenter : Presenter<BackupActivity>(), AnkoLogger {
    companion object {
        const val NAME_FOLDER = "Backup"
        private const val NAME_TEMPLATE = "PaNovel-Backup-##.zip"
        val NAME_FORMAT = NAME_TEMPLATE.replace("##", "%d")
        private val NAME_MATCHER = Regex(NAME_TEMPLATE.replace("##", "(\\d+)"))
        val NAME_PATTERN = NAME_MATCHER.pattern
        val FILENAME_FILTER = FilenameFilter { _, name ->
            name.matches(NAME_MATCHER)
        }
    }

    private val ctx = App.ctx

    private val backupManager = BackupManager()
    private val backupHelperMap: Map<Int, BackupHelper> = mapOf(R.id.rbDefaultWebDav to BackupWebDavHelper())

    fun start() {
        view?.doAsync({ e ->
            val message = "寻找路径失败，"
            Reporter.post(message, e)
            view?.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            val baseFile = File(LocationSettings.backupLocation)
                    .apply { exists() || mkdirs() }
                    .takeIf { it.canWrite() }
                    ?: ctx.filesDir
                            .resolve(NAME_FOLDER)
                            .apply { exists() || mkdirs() }
            val indexList: List<Int> = baseFile.list(FILENAME_FILTER)
                    .map {
                        val (index) = it.pick(NAME_PATTERN)
                        index.toInt()
                    }.sorted()
            val max = indexList.lastOrNull() ?: 1
            val next = max + 1
            val defaultOldName = String.format(Locale.ENGLISH, NAME_FORMAT, max)
            val defaultOldUri = baseFile.resolve(defaultOldName).let { Uri.fromFile(it) }.toString()
            val defaultNewName = String.format(Locale.ENGLISH, NAME_FORMAT, next)
            val defaultNewUri = baseFile.resolve(defaultNewName).let { Uri.fromFile(it) }.toString()

            uiThread {
                view?.showDefaultPath(defaultOldUri, defaultNewUri)
            }

            val defaultOtherName = String.format(Locale.ENGLISH, NAME_FORMAT, 1)
            val defaultOtherUri = Environment.getExternalStorageDirectory()
                    .resolve(defaultOtherName)
                    .let { Uri.fromFile(it) }
                    .toString()
            uiThread {
                view?.showOtherPath(defaultOtherUri)
            }
            backupHelperMap.forEach { entry ->
                if (entry.value.ready()) {
                    view?.showBackupHint(entry.key, entry.value.configPreview())
                }
            }
        }
    }

    fun import() {
        view?.doAsync({ e ->
            val message = "导入失败，"
            error(message, e)
            Reporter.post(message, e)
            view?.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            val options = view.notNullOrReport().getCheckedOption()
            val restore: (File) -> Unit = view.notNullOrReport().getSelectedId().let { backupHelperMap[it] }?.let { backupHelper ->
                if (backupHelper.ready()) {
                    debug {
                        "import: ${backupHelper.type}"
                    }
                    backupHelper::restore
                } else {
                    uiThread {
                        it.startConfig(backupHelper)
                    }
                    throw IllegalStateException("先前往配置")
                }
            } ?: run {
                val uri: Uri = view.notNullOrReport().getSelectPath()
                debug {
                    "import: $uri"
                }
                return@run { tempFile: File ->
                    try {
                        ctx.contentResolver.openInputStream(uri)
                    } catch (e: FileNotFoundException) {
                        if (e.message?.contains("Permission denied") == true) {
                            uiThread {
                                it.requestPermissions()
                            }
                            throw IllegalStateException("没有权限，", e)
                        } else {
                            throw IOException("文件不存在或不可读", e)
                        }
                    }.notNullOrReport().use { input ->
                        debug { "开始导入，" }
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                            output.flush()
                        }
                    }
                }
            }
            val result = backupManager.import(options, restore)
            uiThread {
                it.showImportSuccess(result)
            }
        }
    }

    fun export() {
        view?.doAsync({ e ->
            val message = "导出失败，"
            error(message, e)
            view?.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            val options = view.notNullOrReport().getCheckedOption()
            val backup: (File) -> Unit = view.notNullOrReport().getSelectedId().let { backupHelperMap[it] }?.let { backupHelper ->
                if (backupHelper.ready()) {
                    debug {
                        "export: ${backupHelper.type}"
                    }
                    backupHelper::backup
                } else {
                    uiThread {
                        it.startConfig(backupHelper)
                    }
                    throw IllegalStateException("先前往配置")
                }
            } ?: run {
                val uri: Uri = view.notNullOrReport().getSelectPath()
                debug {
                    "export: $uri"
                }
                return@run { tempFile: File ->
                    try {
                        ctx.contentResolver.openOutputStream(uri)
                    } catch (e: FileNotFoundException) {
                        if (e.message?.contains("Permission denied") == true) {
                            uiThread {
                                it.requestPermissions()
                            }
                            throw IllegalStateException("没有权限，", e)
                        } else {
                            throw IOException("文件不可写", e)
                        }
                    } catch (e: SecurityException) {
                        uiThread {
                            it.requestPermissions()
                        }
                        throw IllegalStateException("没有权限，", e)
                    }.notNullOrReport().use { output ->
                        // 这里貌似不会抛没权限的异常，
                        debug { "开始导出，" }
                        tempFile.inputStream().use { input ->
                            input.copyTo(output)
                            output.flush()
                        }
                    }
                }
            }
            val result = backupManager.export(options, backup)
            uiThread {
                it.showExportSuccess(result)
            }
        }
    }

    fun getHelper(id: Int): BackupHelper? {
        return backupHelperMap[id]
    }
}