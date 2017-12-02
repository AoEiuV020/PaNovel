package cc.aoeiuv020.reader.complex.page

import cc.aoeiuv020.reader.TextRequester
import cc.aoeiuv020.reader.complex.ComplexReader

/**
 * Created by newbiechen on 17-5-29.
 * 网络页面加载器
 */

class NetPageLoader(reader: ComplexReader, pageView: PageView, private val requester: TextRequester) : PageLoader(reader, pageView) {
    companion object {
        private val TAG = "PageFactory"
    }

    //初始化书籍
    override fun openBook() {
        super.openBook()
//        isBookOpen = true
        mStatus = STATUS_FINISH
//        chapterList = reader.chapterList
//        //提示加载下面的章节
//        loadCurrentChapter()
    }

    override fun loadPageList(chapter: Int): List<TxtPage>? {

        // TODO: 需要改成异步，
        val (list) = requester.request(chapter, false)


        return loadPages(reader.chapterList[chapter].name, list.iterator())
    }

    //装载上一章节的内容
    override fun prevChapter(): Boolean {
//
//        val hasPrev = super.prevChapter()
//        if (!hasPrev) return false
//
//        if (mStatus == PageLoader.STATUS_FINISH) {
//            loadCurrentChapter()
//            return true
//        } else if (mStatus == PageLoader.STATUS_LOADING) {
//            loadCurrentChapter()
//            return false
//        }
        return false
    }

    //装载下一章节的内容
    override fun nextChapter(): Boolean {
//        val hasNext = super.nextChapter()
//        if (!hasNext) return false
//
//        if (mStatus == PageLoader.STATUS_FINISH) {
//            loadNextChapter()
//            return true
//        } else if (mStatus == PageLoader.STATUS_LOADING) {
//            loadCurrentChapter()
//            return false
//        }
        return false
    }

    //跳转到指定章节
    override fun skipToChapter(pos: Int) {
        super.skipToChapter(pos)
//
//        //提示章节改变，需要下载
//        loadCurrentChapter()
    }
//
//    private fun loadCurrentChapter() {
//    }
//
//    private fun loadNextChapter() {
//        //提示加载下一章
//        if (mPageChangeListener != null) {
//            //提示加载当前章节和后面3个章节
//            val current = mCurChapterPos + 1
//            var next = mCurChapterPos + 3
//            if (next > chapterList.size) {
//                next = chapterList.size
//            }
//            mPageChangeListener.onLoadChapter(chapterList.subList(current, next), mCurChapterPos)
//        }
//    }
}

