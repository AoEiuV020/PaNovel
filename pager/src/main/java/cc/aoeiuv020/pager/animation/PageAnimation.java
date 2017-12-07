package cc.aoeiuv020.pager.animation;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import cc.aoeiuv020.pager.PagerAnimation;

/**
 * Created by newbiechen on 17-7-24.
 * 翻页动画抽象类
 */

@SuppressWarnings("All")
public abstract class PageAnimation implements PagerAnimation {
    //正在使用的View
    protected View mView;
    //滑动装置
    protected Scroller mScroller;
    //监听器
    protected OnPageChangeListener mListener;
    //移动方向
    protected Direction mDirection = Direction.NONE;

    protected boolean isRunning = false;

    //背景的尺寸，也就是整个视图的尺寸
    protected int mBackgroundWidth;
    protected int mBackgroundHeight;
    //屏幕的间距
    protected int mMarginWidth;
    protected int mMarginHeight;
    //内容的尺寸，背景尺寸减margins,
    protected int mViewWidth;
    protected int mViewHeight;
    //起始点
    protected float mStartX;
    protected float mStartY;
    //触碰点
    protected float mTouchX;
    protected float mTouchY;
    //上一个触碰点
    protected float mLastX;
    protected float mLastY;

    public PageAnimation(int w, int h, View view, OnPageChangeListener listener) {
        this(w, h, new Margins(), view, listener);
    }

    public PageAnimation(int w, int h, Margins margins, View view, OnPageChangeListener listener) {
        mBackgroundWidth = w;
        mBackgroundHeight = h;

        mMarginWidth = margins.getLeft();
        mMarginHeight = margins.getTop();

        mViewWidth = mBackgroundWidth - margins.getLeft() - margins.getRight();
        if (mViewWidth < 1) mViewWidth = 1;
        mViewHeight = mBackgroundHeight - margins.getTop() - margins.getBottom();
        if (mViewHeight < 1) mViewHeight = 1;

        mView = view;
        mListener = listener;

        mScroller = new Scroller(mView.getContext(), new LinearInterpolator());
    }

    public void setStartPoint(float x, float y) {
        mStartX = x;
        mStartY = y;

        mLastX = mStartX;
        mLastY = mStartY;
    }

    public void setTouchPoint(float x, float y) {
        mLastX = mTouchX;
        mLastY = mTouchY;

        mTouchX = x;
        mTouchY = y;
    }

    public boolean isRunning() {
        return isRunning;
    }

    /**
     * 开启翻页动画
     */
    public void startAnim() {
        if (isRunning) {
            return;
        }
        isRunning = true;
    }

    public Direction getDirection() {
        return mDirection;
    }

    public void setDirection(Direction direction) {
        mDirection = direction;
    }

    /**
     * 点击事件的处理
     *
     * @param event
     */
    public abstract boolean onTouchEvent(MotionEvent event);

    /**
     * 绘制图形
     *
     * @param canvas
     */
    public abstract void draw(Canvas canvas);

    /**
     * 滚动动画
     * 必须放在computeScroll()方法中执行
     */
    public abstract void scrollAnim();

    /**
     * 取消动画
     */
    public abstract void abortAnim();

    /**
     * 获取背景板
     *
     * @return
     */
    public abstract Canvas getBgCanvas();

    /**
     * 获取内容显示版面
     */
    public abstract Canvas getConentCanvas();

    protected void drawCurrent() {
        Canvas bgCanvas = getBgCanvas();
        Canvas contentCanvas = getConentCanvas();
        contentCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        mListener.drawCurrent(bgCanvas, contentCanvas);
        if (this instanceof HorizonPageAnim) {
            ((HorizonPageAnim) this).copyContent(bgCanvas);
        }
        mView.postInvalidate();
    }

    protected boolean drawPrev() {
        boolean hasPrev = mListener.hasPrev();
        if (hasPrev) {
            if (this instanceof HorizonPageAnim) {
                ((HorizonPageAnim) this).changePage();
            }
            drawCurrent();
        }
        return hasPrev;
    }

    protected boolean drawNext() {
        boolean hasNext = mListener.hasNext();
        if (hasNext) {
            if (this instanceof HorizonPageAnim) {
                ((HorizonPageAnim) this).changePage();
            }
            drawCurrent();
        }
        return hasNext;
    }

    void pageCancel() {
        mListener.pageCancel();
    }

    @Override
    public void refresh() {
        drawCurrent();
        abortAnim();
    }

    public enum Direction {
        NONE(true), NEXT(true), PRE(true), UP(false), DOWN(false);

        public final boolean isHorizontal;

        Direction(boolean isHorizontal) {
            this.isHorizontal = isHorizontal;
        }
    }

    public interface OnPageChangeListener {
        void drawCurrent(Canvas backgroundCanvas, Canvas nextCanvas);

        boolean hasPrev();

        boolean hasNext();

        void pageCancel();
    }

}
