package cc.aoeiuv020.reader.complex.page;

import android.support.annotation.Nullable;

import java.util.List;

import cc.aoeiuv020.reader.Text;
import cc.aoeiuv020.reader.TextRequester;
import cc.aoeiuv020.reader.complex.ComplexReader;

/**
 * Created by newbiechen on 17-5-29.
 * 网络页面加载器
 */

public class NetPageLoader extends PageLoader {
    private static final String TAG = "PageFactory";
    private final TextRequester requester;

    public NetPageLoader(ComplexReader reader, PageView pageView, TextRequester requester) {
        super(reader, pageView);
        this.requester = requester;
    }

    //初始化书籍
    @Override
    public void openBook() {
        super.openBook();
        isBookOpen = true;
        mChapterList = reader.getChapterList();
        //提示加载下面的章节
        loadCurrentChapter();
    }

//    private List<TxtChapter> convertTxtChapter(List<BookChapterBean> bookChapters) {
//        List<TxtChapter> txtChapters = new ArrayList<>(bookChapters.size());
//        for (BookChapterBean bean : bookChapters) {
//            TxtChapter chapter = new TxtChapter();
//            chapter.bookId = bean.getBookId();
//            chapter.title = bean.getTitle();
//            chapter.link = bean.getLink();
//            txtChapters.add(chapter);
//        }
//        return txtChapters;
//    }

    @Nullable
    @Override
    protected List<TxtPage> loadPageList(int chapter) {

        // TODO: 需要改成异步，
        Text text = requester.request(chapter, false);


        return loadPages(reader.getChapterList().get(chapter).getName(), text.getList().iterator());
    }

    //装载上一章节的内容
    @Override
    boolean prevChapter() {

        boolean hasPrev = super.prevChapter();
        if (!hasPrev) return false;

        if (mStatus == STATUS_FINISH) {
            loadCurrentChapter();
            return true;
        } else if (mStatus == STATUS_LOADING) {
            loadCurrentChapter();
            return false;
        }
        return false;
    }

    //装载下一章节的内容
    @Override
    boolean nextChapter() {
        boolean hasNext = super.nextChapter();
        if (!hasNext) return false;

        if (mStatus == STATUS_FINISH) {
            loadNextChapter();
            return true;
        } else if (mStatus == STATUS_LOADING) {
            loadCurrentChapter();
            return false;
        }
        return false;
    }

    //跳转到指定章节
    public void skipToChapter(int pos) {
        super.skipToChapter(pos);

        //提示章节改变，需要下载
        loadCurrentChapter();
    }

    private void loadCurrentChapter() {
        requester.lazyRequest(mCurChapterPos, false).subscribe(o -> {
            openChapter();
        });
//        if (mPageChangeListener != null) {
//            List<TxtChapter> bookChapters = new ArrayList<>(5);
//            //提示加载当前章节和前面两章和后面两章
//            int current = mCurChapterPos;
//            bookChapters.add(mChapterList.get(current));
//
//            //如果当前已经是最后一章，那么就没有必要加载后面章节
//            if (current != mChapterList.size()) {
//                int begin = current + 1;
//                int next = begin + 2;
//                if (next > mChapterList.size()) {
//                    next = mChapterList.size();
//                }
//                bookChapters.addAll(mChapterList.subList(begin, next));
//            }
//
//            //如果当前已经是第一章，那么就没有必要加载前面章节
//            if (current != 0) {
//                int prev = current - 2;
//                if (prev < 0) {
//                    prev = 0;
//                }
//                bookChapters.addAll(mChapterList.subList(prev, current));
//            }
//            mPageChangeListener.onLoadChapter(bookChapters, mCurChapterPos);
//        }
    }

    private void loadNextChapter() {
        //提示加载下一章
        if (mPageChangeListener != null) {
            //提示加载当前章节和后面3个章节
            int current = mCurChapterPos + 1;
            int next = mCurChapterPos + 3;
            if (next > mChapterList.size()) {
                next = mChapterList.size();
            }
            mPageChangeListener.onLoadChapter(mChapterList.subList(current, next), mCurChapterPos);
        }
    }
//
//    @Override
//    public void setChapterList(List<BookChapterBean> bookChapters) {
//        if (bookChapters == null) return;
//
//        mChapterList = convertTxtChapter(bookChapters);
//
//        if (mPageChangeListener != null) {
//            mPageChangeListener.onCategoryFinish(mChapterList);
//        }
//    }
}

