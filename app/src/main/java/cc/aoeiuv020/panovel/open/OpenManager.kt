package cc.aoeiuv020.panovel.open

import android.content.Context
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.search.FuzzySearchActivity
import cc.aoeiuv020.panovel.share.Share
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.error
import org.jetbrains.anko.uiThread
import java.net.MalformedURLException
import java.net.URL

/**
 * 应用内打开相关的，
 * Created by AoEiuV020 on 2018.03.07-21:40:28.
 */
object OpenManager : AnkoLogger {
    fun open(ctx: Context, str: String, openListener: OpenListener) {
        try {
            val url = URL(str).toString()
            switch(ctx, url, openListener)
        } catch (e: MalformedURLException) {
            // 打开的不是网址，就直接精确搜索，
            FuzzySearchActivity.start(ctx, str)
        }
    }

    private fun switch(ctx: Context, url: String, openListener: OpenListener) {
        openListener.onLoading(ctx.getString(R.string.judging))
        if (Share.check(url)) {
            // 如果是书单分享的地址，直接添加书单，
            openListener.onLoading(ctx.getString(R.string.book_list_downloading))
            ctx.doAsync({ e ->
                val message = "获取书单失败，"
                Reporter.post(message, e)
                error(message, e)
                openListener.onError(message, e)
            }) {
                val count = Share.receiveBookList(url)
                uiThread {
                    openListener.onBookListReceived(count)
                }
            }
        } else {
            // 如果可以从地址得到小说对象，打开详情页，
            ctx.doAsync({ e ->
                val message = "不支持的地址或格式，"
                Reporter.post(message, e)
                error(message, e)
                openListener.onError(message, e)
            }) {
                val novel = DataManager.getNovelFromUrl(url)
                uiThread {
                    openListener.onNovelOpened(novel)
                }
            }
        }
    }

    interface OpenListener {
        fun onLoading(status: String)
        fun onBookListReceived(count: Int)
        fun onError(message: String, e: Throwable)
        fun onNovelOpened(novel: Novel)

    }
}