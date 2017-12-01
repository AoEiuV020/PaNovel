package cc.aoeiuv020.reader

import android.content.Context
import cc.aoeiuv020.reader.simple.SimpleConfig

/**
 *
 * Created by AoEiuV020 on 2017.12.01-02:13:39.
 */
interface INovelReader {
    val ctx: Context

    var novel: Novel

    var chapterChangeListener: ChapterChangeListener?
    var menuListener: MenuListener?

    var requester: TextRequester
    var chapterList: List<Chapter>

    var currentChapter: Int
    var textProgress: Int
    val maxTextProgress: Int

    val config: SimpleConfig

    fun refreshCurrentChapter()

    fun onDestroy()
}

abstract class BaseNovelReader(override var novel: Novel, override var requester: TextRequester, override val config: SimpleConfig) : INovelReader {
    override var chapterChangeListener: ChapterChangeListener? = null
    override var menuListener: MenuListener? = null
    override var currentChapter: Int = 0
    override var chapterList: List<Chapter> = emptyList()
}

