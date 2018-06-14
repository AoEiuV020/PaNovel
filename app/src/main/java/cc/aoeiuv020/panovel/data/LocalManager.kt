package cc.aoeiuv020.panovel.data

import android.content.Context
import android.support.annotation.UiThread
import android.support.annotation.WorkerThread
import cc.aoeiuv020.irondb.Iron
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.local.LocalNovelType
import cc.aoeiuv020.panovel.local.Previewer
import cc.aoeiuv020.panovel.local.TextExporter
import cc.aoeiuv020.panovel.local.TextProvider
import java.io.File
import java.io.InputStream

/**
 * 统一管理本地文件，
 *
 * Created by AoEiuV020 on 2018.06.12-20:16:51.
 */
class LocalManager(ctx: Context) {

    // 所有临时文件都保存在/data/data/cc.aoeiuv020.panovel/cache/local
    // 为了线程安全搞的，但貌似并不需要，一次只会导入一本，
    private val cache = Iron.db(ctx.cacheDir).sub(KEY_LOCAL)
    // 所有导入的小说都保存在/data/data/cc.aoeiuv020.panovel/files/local
    private val files = Iron.db(ctx.filesDir).sub(KEY_LOCAL)

    @WorkerThread
    fun preview(input: InputStream, uri: String): Previewer {
        // 为了支持各种输入流，先复制一遍，得到文件，之后所有操作基于文件，
        val fileWrapper = cache.file(KEY_TEMP_FILE)
        fileWrapper.use { file ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return Previewer(fileWrapper, uri)
    }

    // TODO: 统一导入导出的形式，
    @UiThread
    fun exportText(ctx: Context, novelManager: NovelManager) =
            TextExporter.export(ctx, novelManager)

    fun getNovelProvider(novel: Novel): NovelProvider {
        return when (LocalNovelType.values().first { it.suffix == novel.site }) {
            LocalNovelType.TEXT -> TextProvider(novel)
            LocalNovelType.EPUB -> TODO()
        }
    }

    /**
     * 导入的小说决定好了小说名和作者名就可以移到内部指定位置，以后就读这个文件了，
     */
    fun saveNovel(from: File, suffix: String, author: String, name: String): File {
        val fileName = "$name-$author$suffix"
        return files.file(fileName).use { to ->
            from.copyTo(to, overwrite = true)
            to
        }
    }

    companion object {
        const val KEY_LOCAL = "local"
        const val KEY_TEMP_FILE = "file.tmp"
    }
}