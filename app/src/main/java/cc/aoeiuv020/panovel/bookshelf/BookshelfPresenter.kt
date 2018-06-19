package cc.aoeiuv020.panovel.bookshelf

import cc.aoeiuv020.panovel.Presenter
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.data.NovelManager
import cc.aoeiuv020.panovel.report.Reporter
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.error
import org.jetbrains.anko.uiThread

/**
 *
 * Created by AoEiuV020 on 2017.10.14-21:54.
 */
class BookshelfPresenter : Presenter<BookshelfFragment>() {

    private fun requestBookshelf() {
        view?.doAsync({ e ->
            val message = "获取书架列表失败，"
            Reporter.post(message, e)
            error(message, e)
            view?.activity?.runOnUiThread {
                view?.showError(message, e)
            }
        }) {
            // TODO: 异步顺序不定，可能在退出阅读页面时的保存进度还没开始就先开始列出书架，导致内容不正确，
            val list = DataManager.listBookshelf()
            uiThread {
                view?.showNovelList(list)
            }
        }
    }

    fun askUpdate(novelList: List<NovelManager>) {
        view?.doAsync({ e ->
            val message = "询问服务器小说列表更新失败，"
            Reporter.post(message, e)
            error(message, e)
            view?.activity?.runOnUiThread {
                view?.askUpdateError(message, e)
            }
        }) {
            val resultList = DataManager.askUpdate(novelList)
            uiThread {
                view?.showAskUpdateResult(resultList)
            }
        }
    }

    fun refresh() {
        requestBookshelf()
    }
}

