package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.data.NovelProvider
import java.io.File

/**
 * Created by AoEiuV020 on 2018.06.13-15:38:49.
 */
abstract class LocalNovelProvider(
        protected val file: File
) : NovelProvider {
    override fun getContentUrl(chapter: NovelChapter): String {
        return getDetailUrl()
    }

    override fun getDetailUrl(): String {
        return "file://${file.absoluteFile.canonicalPath}"
    }
}