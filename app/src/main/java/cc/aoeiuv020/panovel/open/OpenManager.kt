package cc.aoeiuv020.panovel.open

import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelContext
import cc.aoeiuv020.panovel.detail.NovelDetailActivity
import cc.aoeiuv020.panovel.local.BookList
import cc.aoeiuv020.panovel.main.MainActivity
import cc.aoeiuv020.panovel.search.RefineSearchActivity
import cc.aoeiuv020.panovel.share.Share
import cc.aoeiuv020.panovel.util.async
import cc.aoeiuv020.panovel.util.loading
import io.reactivex.Observable
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import java.net.MalformedURLException
import java.net.URL

/**
 * 应用内打开相关的，
 * Created by AoEiuV020 on 2018.03.07-21:40:28.
 */
object OpenManager : AnkoLogger {
    fun open(context: MainActivity, str: String) {
        try {
            val url = URL(str).toString()
            switch(context, url)
        } catch (e: MalformedURLException) {
            // 打开的不是网址，就直接精确搜索，
            RefineSearchActivity.start(context, str)
        }
    }

    private fun switch(context: MainActivity, url: String) {
        context.apply {
            loading(progressDialog, getString(R.string.judging))
        }
        if (Share.check(url)) {
            // 如果是书单分享的地址，直接添加书单，
            context.apply {
                loading(progressDialog, getString(R.string.book_list_downloading))
                Observable.fromCallable {
                    val bookList = Share.receiveBookList(url)
                    BookList.put(bookList)
                    bookList.list.size
                }.async().subscribe({ size ->
                    bookListFragment.refresh()
                    progressDialog.dismiss()
                    showMessage("添加书单，共${size}本，")
                }, { e ->
                    val message = "获取书单失败，"
                    error(message, e)
                    showError(message, e)
                })
            }
        } else {
            // 如果可以从地址得到小说对象，打开详情页，
            Observable.fromCallable {
                // 地址格式正确但爬取出错也一样跳到下面的不支持，
                NovelContext.getNovelContextByUrl(url).getNovelItem(url)
            }.async().subscribe({ novelItem ->
                NovelDetailActivity.start(context, novelItem)
                context.progressDialog.dismiss()
            }, { e ->
                val message = "不支持的地址或格式，"
                error(message, e)
                context.progressDialog.dismiss()
                context.showMessage(message)
            })
        }
    }
}