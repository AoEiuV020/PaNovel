package cc.aoeiuv020.panovel.data

import android.content.Context
import android.support.annotation.UiThread
import android.support.annotation.WorkerThread
import cc.aoeiuv020.base.jar.interrupt
import cc.aoeiuv020.base.jar.pick
import cc.aoeiuv020.irondb.Iron
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.local.LocalNovelType
import cc.aoeiuv020.panovel.local.Previewer
import cc.aoeiuv020.panovel.local.TextExporter
import cc.aoeiuv020.panovel.local.TextProvider
import cc.aoeiuv020.panovel.util.notNullOrReport
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import java.io.File
import java.io.InputStream
import java.nio.charset.UnsupportedCharsetException

/**
 * 统一管理本地文件，
 *
 * Created by AoEiuV020 on 2018.06.12-20:16:51.
 */
class LocalManager(ctx: Context) : AnkoLogger {

    // 所有临时文件都保存在/data/data/cc.aoeiuv020.panovel/cache/local
    // 为了线程安全搞的，但貌似并不需要，一次只会导入一本，
    private val cache = Iron.db(ctx.cacheDir).sub(KEY_LOCAL)
    // 所有导入的小说都保存在/data/data/cc.aoeiuv020.panovel/files/local
    private val files = Iron.db(ctx.filesDir).sub(KEY_LOCAL)

    @WorkerThread
    fun importLocalNovel(input: InputStream, uri: String, requestInput: (Int, String) -> String?): Pair<Novel, List<NovelChapter>> {
        debug {
            "importLocalNovel from: $uri"
        }
        // 锁住临时文件，一次只能导入一本小说，
        return cache.file(KEY_TEMP_FILE).use { file ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
            // uri基本都有带文件路径，可以传进去用于判断小说文件类型，没有的话，就没有吧，让用户选择，
            importLocalNovel(uri, Previewer(file, uri), requestInput).also {
                // 临时文件用完就删，
                file.delete()
            }
        }
    }


    @WorkerThread
    private fun importLocalNovel(uri: String, previewer: Previewer, requestInput: (Int, String) -> String?): Pair<Novel, List<NovelChapter>> {
        // 传入的ctx用于弹对话框让用户传入可能需要的小说格式，编码，作者名，小说名，

        @Suppress("UNUSED_VARIABLE")
        val defaultType = previewer.guessType() ?: LocalNovelType.TEXT
        // TODO: 暂且只支持.txt,
        val actualType = LocalNovelType.TEXT
        previewer.type = actualType
/*
        val actualType = LocalNovelType.values().firstOrNull {
            it.suffix == input(ctx, title = R.string.input_file_type, default = defaultType.suffix)
        } ?: interrupt("没有文件类型，")
*/
        debug {
            "importLocalNovel file type: ${actualType.suffix}"
        }

        if (actualType == LocalNovelType.TEXT) {
            // 只有纯文本小说需要指定编码，
            val defaultCharset = previewer.guessCharset() ?: "unknown"
            val actualCharset = requestInput(R.string.input_charset, defaultCharset)?.let {
                try {
                    charset(it)
                } catch (e: UnsupportedCharsetException) {
                    interrupt("不支持的文件编码<$it>，")
                }
            } ?: interrupt("没有文件编码，")
            previewer.charset = actualCharset
            debug {
                "importLocalNovel file charset: $actualCharset"
            }
        }

        val parser = previewer.getParser()
        // 一次性得到可能能得到的作者名，小说名，简介，
        parser.parse()

        debug {
            "importLocalNovel parse: <${parser.name}-${parser.author}${parser.type.suffix}, ${parser.introduction}, ${parser.chapters.size}>"
        }
        val suffix = parser.type.suffix
        val defaultName = try {
            // 提取uri最后一节，一般是就是文件名，当成默认的作者名和小说名，
            uri.pick("/([^/]+)$").first()
        } catch (e: Exception) {
            "null"
        }
        val author = requestInput(R.string.input_author, parser.author
                ?: defaultName)
                ?: interrupt("没有作者名，")
        val name = requestInput(R.string.input_name, parser.name
                ?: defaultName)
                ?: interrupt("没有小说名，")

        // 最终导入的小说就永久保存在这里了，
        val file = saveNovel(previewer.file, suffix, author, name)
        val novel = Novel(
                site = suffix,
                author = author,
                name = name,
                detail = file.absoluteFile.canonicalPath,
                // 刚导入的小说一定要放在书架上，否则找不到，
                bookshelf = true,
                // 这里保存纯文本小说的编码，如果是epub不需要编码也要随便给个值，毕竟是用这个是否空判断是否需要请求小说详情，
                chapters = parser.requester ?: "null"
        )
        TextProvider.update(novel, parser)
        return novel to parser.chapters.notNullOrReport().map { NovelChapter(name = it.name, extra = it.extra) }
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