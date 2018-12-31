package cc.aoeiuv020.panovel.settings

import cc.aoeiuv020.panovel.backup.BackupPresenter
import cc.aoeiuv020.panovel.local.NovelExporter
import cc.aoeiuv020.panovel.util.Delegates
import cc.aoeiuv020.panovel.util.Pref

/**
 * Created by AoEiuV020 on 2018.12.31-20:41:11.
 */
@Suppress("unused")
object LocationSettings : Pref {
    override val name: String
        get() = "Location"
    var downloadLocation: String by Delegates.string("")
    var cacheLocation: String by Delegates.string(ctx.cacheDir.absolutePath)
    var backupLocation: String by Delegates.string((
            // 优先SD卡，不可用就filesDir,
            ctx.getExternalFilesDir(null)
                    ?.resolve(BackupPresenter.NAME_FOLDER)
                    ?.apply { exists() || mkdirs() }
                    ?.takeIf { it.canWrite() }
                    ?: ctx.filesDir.resolve(BackupPresenter.NAME_FOLDER)
            ).absolutePath
    )
    var exportLocation: String by Delegates.string((
            // 优先SD卡，不可用就filesDir,
            ctx.getExternalFilesDir(null)
                    ?.resolve(NovelExporter.NAME_FOLDER)
                    ?.apply { exists() || mkdirs() }
                    ?.takeIf { it.canWrite() }
                    ?: ctx.filesDir.resolve(NovelExporter.NAME_FOLDER)
            ).absolutePath
    )
}