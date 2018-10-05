package cc.aoeiuv020.panovel.open

import android.content.Context
import android.net.Uri
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.local.ImportRequireValue
import cc.aoeiuv020.panovel.local.LocalNovelType
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.share.Share
import cc.aoeiuv020.panovel.util.uiInput
import cc.aoeiuv020.panovel.util.uiSelect
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.error
import org.jetbrains.anko.uiThread

/**
 * 应用内打开相关的，
 * Created by AoEiuV020 on 2018.03.07-21:40:28.
 */
object OpenManager : AnkoLogger {
    fun open(ctx: Context, str: String, openListener: OpenListener) {
        // Uri.parse没有检测，不会抛异常，不正常的uri会在getScheme返回null,
        open(ctx, Uri.parse(str), openListener)
    }

    fun open(ctx: Context, uri: Uri, openListener: OpenListener) {
        switch(ctx, uri, openListener)
    }

    private fun switch(ctx: Context, uri: Uri, openListener: OpenListener) {
        openListener.onLoading(ctx.getString(R.string.judging))
        when {
            uri.scheme == null -> {
                openListener.onOtherCase(uri.toString())
            }
            !uri.scheme.startsWith("http") -> {
                // 协议不是http或https的话统统当成本地小说打开，
                openListener.onLoading(ctx.getString(R.string.local_novel_importing))
                ctx.doAsync({ e ->
                    val message = "导入本地小说失败，"
                    Reporter.post(message, e)
                    error(message, e)
                    openListener.onError(message, e)
                }) {
                    val novel: Novel = DataManager.importLocalNovel(ctx, uri) { value, default ->
                        if (value == ImportRequireValue.TYPE) {
                            val types = LocalNovelType.values()
                            val items = types.map {
                                when (it) {
                                    LocalNovelType.TEXT -> R.string.select_item_text
                                    LocalNovelType.EPUB -> R.string.select_item_epub
                                }.let { ctx.getString(it) }
                            }.toTypedArray()
                            val defaultIndex = types.indexOfFirst {
                                it.suffix == default
                            }
                            ctx.uiSelect(ctx.getString(R.string.file_type), items, defaultIndex)?.let { selectIndex ->
                                types[selectIndex].suffix
                            }
                        } else {
                            val name = when (value) {
                            // 输入文件类型不会走这里，
                                ImportRequireValue.TYPE -> R.string.file_type
                                ImportRequireValue.CHARSET -> R.string.file_charset
                                ImportRequireValue.AUTHOR -> R.string.author
                                ImportRequireValue.NAME -> R.string.name
                            }.let { ctx.getString(it) }
                            ctx.uiInput(name, default)
                        }
                    }
                    uiThread {
                        openListener.onLocalNovelImported(novel)
                    }
                }
            }
            Share.check(uri.toString()) -> {
                // 如果是书单分享的地址，直接添加书单，
                openListener.onLoading(ctx.getString(R.string.book_list_downloading))
                ctx.doAsync({ e ->
                    val message = "获取书单失败，"
                    Reporter.post(message, e)
                    error(message, e)
                    openListener.onError(message, e)
                }) {
                    val count = Share.receiveBookList(uri.toString())
                    uiThread {
                        openListener.onBookListReceived(count)
                    }
                }
            }
            else -> // 如果可以从地址得到小说对象，打开详情页，
                ctx.doAsync({ e ->
                    val message = "不支持的地址或格式，"
                    Reporter.post(message, e)
                    error(message, e)
                    openListener.onError(message, e)
                }) {
                    val novel = DataManager.getNovelFromUrl(uri.toString())
                    uiThread {
                        openListener.onNovelOpened(novel)
                    }
                }
        }
    }

    interface OpenListener {
        fun onLoading(status: String)
        fun onOtherCase(str: String)
        fun onBookListReceived(count: Int)
        fun onError(message: String, e: Throwable)
        fun onNovelOpened(novel: Novel)
        fun onLocalNovelImported(novel: Novel)
    }
}