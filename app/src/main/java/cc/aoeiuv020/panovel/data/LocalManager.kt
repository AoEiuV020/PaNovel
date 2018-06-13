package cc.aoeiuv020.panovel.data

import android.content.Context
import android.support.annotation.UiThread
import android.support.annotation.WorkerThread
import cc.aoeiuv020.irondb.Iron
import cc.aoeiuv020.panovel.local.TextExporter
import cc.aoeiuv020.panovel.local.TextImporter
import java.io.InputStream
import java.nio.charset.Charset

/**
 * 统一管理本地文件，
 *
 * Created by AoEiuV020 on 2018.06.12-20:16:51.
 */
class LocalManager(ctx: Context) {
    // 所有临时文件都保存在/data/data/cc.aoeiuv020.panovel/cache/local
    private val root = Iron.db(ctx.cacheDir).sub("local")

    @WorkerThread
    @Suppress("unused")
    fun importText(input: InputStream, charset: Charset) =
            TextImporter(root.sub(KEY_IMPORTER)).apply {
                importText(input, charset)
            }

    // TODO: 统一导入导出的形式，
    @UiThread
    fun exportText(ctx: Context, novelManager: NovelManager) =
            TextExporter.export(ctx, novelManager)

    companion object {
        const val KEY_IMPORTER = "importer"
        @Suppress("unused")
        const val KEY_TEXT = "text"
    }
}