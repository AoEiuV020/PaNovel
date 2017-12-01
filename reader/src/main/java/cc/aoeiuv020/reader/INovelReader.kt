package cc.aoeiuv020.reader

import android.content.Context

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

    val config: Config

    fun refreshCurrentChapter()

    fun onDestroy()
}

abstract class BaseNovelReader(override var novel: Novel, override var requester: TextRequester, override val config: Config) : INovelReader {
    override var chapterChangeListener: ChapterChangeListener? = null
    override var menuListener: MenuListener? = null
    override var currentChapter: Int = 0
    override var chapterList: List<Chapter> = emptyList()
}

