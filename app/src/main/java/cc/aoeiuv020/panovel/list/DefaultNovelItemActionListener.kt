package cc.aoeiuv020.panovel.list

import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.detail.NovelDetailActivity
import cc.aoeiuv020.panovel.export.TextExporter
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.search.FuzzySearchActivity
import cc.aoeiuv020.panovel.settings.ItemAction
import cc.aoeiuv020.panovel.settings.ItemAction.*
import cc.aoeiuv020.panovel.settings.ListSettings
import cc.aoeiuv020.panovel.text.NovelTextActivity
import org.jetbrains.anko.*

/**
 * Created by AoEiuV020 on 2018.05.23-12:49:51.
 */
open class DefaultNovelItemActionListener(
        private val onError: (String, Throwable) -> Unit
) : NovelItemActionListener, AnkoLogger {
    fun on(enum: ItemAction, vh: NovelViewHolder): Boolean {
        when (enum) {
            ReadLastChapter -> NovelTextActivity.start(vh.ctx, vh.novel, -1)
            ReadContinue -> NovelTextActivity.start(vh.ctx, vh.novel)
            OpenDetail -> NovelDetailActivity.start(vh.ctx, vh.novel)
            RefineSearch -> FuzzySearchActivity.start(vh.ctx, vh.novel)
            Export -> TextExporter.export(vh.ctx, vh.novel)
            AddBookshelf -> vh.addBookshelf() // vh里再反过来调用onStarChanged，
            RemoveBookshelf -> vh.removeBookshelf() // vh里再反过来调用onStarChanged，
            Refresh -> vh.refresh()
            Pinned -> pinned(vh)
            CancelPinned -> cancelPinned(vh)
            MoreAction -> {
                val list = vh.ctx.resources.getStringArray(R.array.content_more_action).toList()
                val actions = listOf(
                        ReadContinue,
                        ReadLastChapter,
                        OpenDetail,
                        RefineSearch,
                        Export,
                        AddBookshelf,
                        RemoveBookshelf,
                        Refresh
                )
                vh.ctx.selector(vh.ctx.getString(R.string.title_more_action), list) { _, i ->
                    on(actions[i], vh)
                }
            }
        // 返回false不消费长按事件，
            None -> return false
        }
        return true
    }

    override fun onDotClick(vh: NovelViewHolder) {
        on(ListSettings.onDotClick, vh)
    }

    override fun onDotLongClick(vh: NovelViewHolder): Boolean {
        return on(ListSettings.onDotLongClick, vh)
    }

    override fun onCheckUpdateClick(vh: NovelViewHolder) {
        on(ListSettings.onCheckUpdateClick, vh)
    }

    override fun onNameClick(vh: NovelViewHolder) {
        on(ListSettings.onNameClick, vh)
    }

    override fun onNameLongClick(vh: NovelViewHolder): Boolean {
        return on(ListSettings.onNameLongClick, vh)
    }

    override fun onLastChapterClick(vh: NovelViewHolder) {
        on(ListSettings.onLastChapterClick, vh)
    }

    override fun onItemClick(vh: NovelViewHolder) {
        on(ListSettings.onItemClick, vh)
    }

    override fun onItemLongClick(vh: NovelViewHolder): Boolean {
        return on(ListSettings.onItemLongClick, vh)
    }

    override fun onStarChanged(vh: NovelViewHolder, star: Boolean) {
        vh.novel.bookshelf = star
        doAsync({ e ->
            val message = "${if (star) "添加" else "删除"}书架《${vh.novel.name}》失败，"
            // 这应该是数据库操作出问题，正常情况不会出现才对，
            // 未知异常统一上报，
            Reporter.post(message, e)
            error(message, e)
            vh.ctx.runOnUiThread {
                onError(message, e)
            }
        }) {
            DataManager.updateBookshelf(vh.novel)
        }

    }

    override fun requireRefresh(vh: NovelViewHolder) {
        doAsync({ e ->
            val message = "刷新小说《${vh.novel.name}》失败，"
            Reporter.post(message, e)
            error(message, e)
            vh.ctx.runOnUiThread {
                // 失败也停止显示正在刷新，
                vh.refreshed(vh.novel)
                onError(message, e)
            }
        }) {
            DataManager.refreshChapters(vh.novel)
            uiThread {
                vh.refreshed(vh.novel)
            }
        }
    }

    private fun pinned(vh: NovelViewHolder) {
        doAsync({ e ->
            val message = "置顶小说《${vh.novel.name}》失败，"
            Reporter.post(message, e)
            error(message, e)
            vh.ctx.runOnUiThread {
                onError(message, e)
            }
        }) {
            DataManager.pinned(vh.novel)
            // TODO: 置顶后刷新列表， 移到开头,
        }
    }

    private fun cancelPinned(vh: NovelViewHolder) {
        doAsync({ e ->
            val message = "取消置顶小说《${vh.novel.name}》失败，"
            Reporter.post(message, e)
            error(message, e)
            vh.ctx.runOnUiThread {
                onError(message, e)
            }
        }) {
            DataManager.cancelPinned(vh.novel)
            // TODO: 取消置顶后刷新列表，
        }
    }
}