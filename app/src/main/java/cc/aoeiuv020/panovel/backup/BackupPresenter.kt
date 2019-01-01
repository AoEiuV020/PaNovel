package cc.aoeiuv020.panovel.backup

import android.net.Uri
import android.os.Environment
import cc.aoeiuv020.panovel.App
import cc.aoeiuv020.panovel.Presenter
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.settings.LocationSettings
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
            val uri: Uri = view?.getSelectPath() ?: return@doAsync
            val options = view?.getCheckedOption() ?: return@doAsync
            debug {
                "import: $uri"
            }
            val result = try {
                ctx.contentResolver.openInputStream(uri)
            } catch (e: FileNotFoundException) {
                if (e.message?.contains("Permission denied") == true) {
                    view?.requestPermissions()
                    throw IllegalStateException("没有权限，", e)
                } else {
                    throw IOException("文件不存在或不可读", e)
                }
            }.use { input ->
                debug { "开始导入，" }
                backupManager.import(input, options)
            }
            uiThread {
                view?.showImportSuccess(result)
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
            val uri: Uri = view?.getSelectPath() ?: return@doAsync
            val options = view?.getCheckedOption() ?: return@doAsync
            debug {
                "export: $uri"
            }
            val result = try {
                ctx.contentResolver.openOutputStream(uri)
            } catch (e: FileNotFoundException) {
                if (e.message?.contains("Permission denied") == true) {
                    view?.requestPermissions()
                    throw IllegalStateException("没有权限，", e)
                } else {
                    throw IOException("文件不可写", e)
                }
            } catch (e: SecurityException) {
                view?.requestPermissions()
                throw IllegalStateException("没有权限，", e)
            }.use { output ->
                // 这里貌似不会抛没权限的异常，
                debug { "开始导出，" }
                backupManager.export(output, options)
            }
            uiThread {
                view?.showExportSuccess(result)
            }
        }
    }
}