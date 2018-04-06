package cc.aoeiuv020.panovel.base.item

import cc.aoeiuv020.panovel.IView
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.NovelDetail
import java.util.*

/**
 *
 * Created by AoEiuV020 on 2017.11.22-10:48:04.
 */
interface BaseItemListView : IView {
    fun showError(message: String, e: Throwable)
}

interface SmallItemView : IView {
    fun showDetail(novelDetail: NovelDetail)
    fun showNewChapterDot()
    fun showChapter(chapters: List<NovelChapter>, progress: Int)
    fun hideProgressBar()
}

interface BigItemView : SmallItemView {
    fun showUpdateTime(updateTime: Date?)
}