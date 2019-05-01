package cc.aoeiuv020.panovel.data

import android.content.Context
import androidx.annotation.WorkerThread
import cc.aoeiuv020.exception.interrupt
import cc.aoeiuv020.irondb.Iron
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.local.ImportRequireValue
import cc.aoeiuv020.panovel.local.LocalNovelProvider
import cc.aoeiuv020.panovel.local.LocalNovelType
import cc.aoeiuv020.panovel.local.Previewer
import cc.aoeiuv020.panovel.util.noCover
import cc.aoeiuv020.panovel.util.notNullOrReport
import cc.aoeiuv020.regex.pick
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import java.io.File
import java.io.InputStream
import java.net.URLDecoder
import java.nio.charset.Charset
import java.nio.charset.UnsupportedCharsetException

/**
 * 统一管理本地文件，
 *
 * Created by AoEiuV020 on 2018.06.12-20:16:51.
 */
class LocalManager(
        private val ctx: Context
) : AnkoLogger {

    // 所有临时文件都保存在/data/data/cc.aoeiuv020.panovel/cache/local
    // 为了线程安全搞的，但貌似并不需要，一次只会导入一本，
    private val cache = Iron.db(ctx.cacheDir).sub(KEY_LOCAL)
    // 所有导入的小说都保存在/data/data/cc.aoeiuv020.panovel/files/local
    private val files = Iron.db(ctx.filesDir).sub(KEY_LOCAL)

    @WorkerThread
    fun importLocalNovel(input: InputStream, uri: String,
                         requestInput: (ImportRequireValue, String) -> String?
    ): Pair<Novel, List<NovelChapter>> {
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
    private fun importLocalNovel(uri: String, previewer: Previewer,
                                 requestInput: (ImportRequireValue, String
                                 ) -> String?): Pair<Novel, List<NovelChapter>> {
        @Suppress("UNUSED_VARIABLE")
        // defaultType要作为单选框的默认值，必须是存在的type,
        val defaultType = previewer.guessType() ?: LocalNovelType.TEXT
        val actualTypeSuffix = requestInput(ImportRequireValue.TYPE, defaultType.suffix)
                ?: interrupt(ctx.getString(R.string.tip_not_select_file_type))
        val actualType = LocalNovelType.values().firstOrNull {
            it.suffix == actualTypeSuffix
        } ?: interrupt(ctx.getString(R.string.tip_no_file_type))
        debug {
            "importLocalNovel file type: ${actualType.suffix}"
        }
        val actualCharset: Charset? = if (actualType == LocalNovelType.TEXT
                || actualType == LocalNovelType.EPUB) {
            // 纯文本小说和epub电子书都需要指定编码，
            // 目前只打算支持这各种小说，这个if没什么必要，但是留着，
            val defaultCharset = previewer.guessCharset(actualType) ?: "unknown"
            val actualCharset = requestInput(ImportRequireValue.CHARSET, defaultCharset)?.let {
                try {
                    charset(it)
                } catch (e: UnsupportedCharsetException) {
                    interrupt(ctx.getString(R.string.tip_not_support_charset, it))
                }
            } ?: interrupt(ctx.getString(R.string.tip_no_charset))
            debug {
                "importLocalNovel file charset: $actualCharset"
            }
            actualCharset
        } else {
            null
        }

        // 一次性得到可能能得到的作者名，小说名，简介，
        val info = previewer.parse(actualType, actualCharset)

        debug {
            "importLocalNovel parse: <${info.name}-${info.author}${actualType.suffix}, ${info.image}, ${info.introduction}, ${info.chapters.size}>"
        }
        val suffix = actualType.suffix
        val defaultName = try {
            // Uri先解码，因为可能系统文件管理器给的uri中文件路径是经过encode的，
            // 有问题也无所谓，只是个默认值，
            URLDecoder.decode(uri, Charsets.UTF_8.name())
                    // 提取uri最后一节，一般是就是文件名，当成默认的作者名和小说名，
                    .pick("/([^/]+)$").first()
        } catch (e: Exception) {
            "null"
        }
        val name = requestInput(ImportRequireValue.NAME, info.name
                ?: defaultName)
                ?: interrupt(ctx.getString(R.string.tip_no_novel_name))
        val author = requestInput(ImportRequireValue.AUTHOR, info.author
        // 没有作者名就用小说名顶一下，当成默认值给用户改，
                ?: name)
                ?: interrupt(ctx.getString(R.string.tip_no_author_name))

        // 最终导入的小说就永久保存在这里了，
        val file = saveNovel(previewer.file, suffix, author, name)
        val novel = Novel(
                site = suffix,
                author = author,
                name = name,
                image = info.image ?: noCover,
                // detail存文件全路径，不存url, file协议的url支持不只一种形式，主要是斜杆/的差别，
                detail = file.absoluteFile.canonicalPath,
                introduction = info.introduction ?: "(null)",
                // 刚导入的小说一定要放在书架上，否则找不到，
                bookshelf = true,
                // 这里保存纯文本小说的编码，epub也需要指定编码，
                chapters = info.requester ?: "null"
        )
        // 更新novel对象中关于章节数据，不能白解析了，
        LocalNovelProvider.update(novel, info)
        return novel to info.chapters.notNullOrReport().map { NovelChapter(name = it.name, extra = it.extra) }
    }

    /**
     * 导入的小说决定好了小说名和作者名就可以移到内部指定位置，以后就读这个文件了，
     */
    private fun saveNovel(from: File, suffix: String, author: String, name: String): File {
        val fileName = "$name-$author$suffix"
        return files.file(fileName).use { to ->
            from.copyTo(to, overwrite = true)
            to
        }
    }

    fun getNovelProvider(novel: Novel): NovelProvider {
        return LocalNovelProvider(novel)
    }

    companion object {
        const val KEY_LOCAL = "local"
        const val KEY_TEMP_FILE = "file.tmp"
    }
}