package cc.aoeiuv020.panovel.list

import cc.aoeiuv020.base.jar.ioExecutorService
import cc.aoeiuv020.exception.interrupt
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.detail.NovelDetailActivity
import cc.aoeiuv020.panovel.local.LocalNovelType
import cc.aoeiuv020.panovel.local.NovelExporter
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.search.FuzzySearchActivity
import cc.aoeiuv020.panovel.settings.ItemAction
import cc.aoeiuv020.panovel.settings.ItemAction.*
import cc.aoeiuv020.panovel.settings.ListSettings
import cc.aoeiuv020.panovel.text.NovelTextActivity
import cc.aoeiuv020.panovel.util.uiInput
import cc.aoeiuv020.panovel.util.uiSelect
import org.jetbrains.anko.*
import java.nio.charset.UnsupportedCharsetException

/**
 * Created by AoEiuV020 on 2018.05.23-12:49:51.
 */
class DefaultNovelItemActionListener(
        private val actionDoneListener: (ItemAction, NovelViewHolder) -> Unit = { _, _ -> },
        private val onError: (String, Throwable) -> Unit
) : NovelItemActionListener, AnkoLogger {
    override val loggerTag: String
        get() = "ItemActionListener"

    fun on(action: ItemAction, vh: NovelViewHolder): Boolean {
        debug { "doing $action at ${vh.novel.name}" }
        when (action) {
            ReadLastChapter -> NovelTextActivity.start(vh.ctx, vh.novel, -1)
            ReadContinue -> NovelTextActivity.start(vh.ctx, vh.novel)
            OpenDetail -> NovelDetailActivity.start(vh.ctx, vh.novel)
            RefineSearch -> FuzzySearchActivity.start(vh.ctx, vh.novel)
            Export -> exportNovel(vh)
            // TODO: 有点混乱不统一，改支之前考虑清楚，主要是有的操作需要更新vh界面，
            AddBookshelf -> vh.addBookshelf() // vh里再反过来调用onStarChanged，
            RemoveBookshelf -> vh.removeBookshelf() // vh里再反过来调用onStarChanged，
            Refresh -> vh.refresh()
            Cache -> download(vh)
            Pinned -> pinned(vh)
            CancelPinned -> cancelPinned(vh)
            CleanCache -> cleanCache(vh)
            CleanData -> cleanData(vh)
            MoreAction -> {
                val list = listOf(
                        R.string.read_continue to ReadContinue,
                        R.string.read_last_chapter to ReadLastChapter,
                        R.string.refresh to Refresh,
                        R.string.open_detail to OpenDetail,

                        R.string.refine_search to RefineSearch,
                        R.string.export to Export,
                        if (vh.novel.bookshelf) {
                            R.string.remove_bookshelf to RemoveBookshelf
                        } else {
                            R.string.add_bookshelf to AddBookshelf
                        },
                        R.string.cache to Cache,
                        R.string.pinned to Pinned,
                        R.string.cancel_pinned to CancelPinned,

                        R.string.clean_cache to CleanCache,
                        R.string.clean_this_novel to CleanData,

                        R.string.history to Refresh
                )
                vh.ctx.selector(vh.ctx.getString(R.string.title_more_action),
                        list.unzip().first.map { vh.ctx.getString(it) }) { _, i ->
                    on(list[i].second, vh)
                }

            }
            // 返回false不消费长按事件，
            None -> return false
        }
        actionDoneListener(action, vh)
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
        val novelManager = vh.novelManager
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
            novelManager.updateBookshelf(star)
        }

    }

    override fun refreshChapters(vh: NovelViewHolder) {
        val novelManager = vh.novelManager
        doAsync({ e ->
            val message = "刷新小说《${vh.novel.name}》失败，"
            Reporter.post(message, e)
            error(message, e)
            vh.ctx.runOnUiThread {
                // 失败也停止显示正在刷新，
                vh.refreshed(novelManager)
                onError(message, e)
            }
        }, ioExecutorService) {
            novelManager.requestChapters(true)
            uiThread {
                vh.refreshed(novelManager)
            }
        }
    }

    private fun download(vh: NovelViewHolder) {
        DataManager.download.askDownload(vh.ctx, vh.novelManager, vh.novel.readAtChapterIndex, true)
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
            vh.novelManager.pinned()
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
            vh.novelManager.cancelPinned()
        }
    }

    private fun cleanCache(vh: NovelViewHolder) {
        doAsync({ e ->
            val message = "清除小说缓存<${vh.novel.bookId}>失败，"
            Reporter.post(message, e)
            error(message, e)
            vh.ctx.runOnUiThread {
                onError(message, e)
            }
        }) {
            vh.novelManager.cleanCache()
        }
    }

    private fun cleanData(vh: NovelViewHolder) {
        doAsync({ e ->
            val message = "清除小说数据<${vh.novel.bookId}>失败，"
            Reporter.post(message, e)
            error(message, e)
            vh.ctx.runOnUiThread {
                onError(message, e)
            }
        }) {
            vh.novelManager.cleanData()
        }
    }

    private fun exportNovel(vh: NovelViewHolder) {
        doAsync({ e ->
            val message = "导出小说<${vh.novel.bookId}>失败，"
            Reporter.post(message, e)
            error(message, e)
            vh.ctx.runOnUiThread {
                onError(message, e)
            }
        }, ioExecutorService) {
            val ctx = vh.ctx
            val types = LocalNovelType.values()
            val items = types.map { type ->
                when (type) {
                    LocalNovelType.TEXT -> R.string.select_item_text
                    LocalNovelType.EPUB -> R.string.select_item_epub
                }.let { ctx.getString(it) }
            }.toTypedArray()
            // 默认导出txt,
            val defaultIndex = 0
            val type = ctx.uiSelect(ctx.getString(R.string.file_type), items, defaultIndex)?.let { selectIndex ->
                types[selectIndex]
            } ?: interrupt(ctx.getString(R.string.tip_no_file_type))
            val charset = if (type == LocalNovelType.TEXT) {
                ctx.uiInput(ctx.getString(R.string.file_charset), Charsets.UTF_8.name())?.let {
                    try {
                        charset(it)
                    } catch (e: UnsupportedCharsetException) {
                        interrupt(ctx.getString(R.string.tip_not_support_charset, it))
                    }
                } ?: interrupt(ctx.getString(R.string.tip_no_charset))
            } else {
                Charsets.UTF_8
            }

            NovelExporter.export(vh.ctx, type, charset, vh.novelManager)
        }
    }

}