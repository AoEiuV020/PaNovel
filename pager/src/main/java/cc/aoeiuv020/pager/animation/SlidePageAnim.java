package cc.aoeiuv020.pager.animation;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

/**
 * Created by newbiechen on 17-7-24.
 */

@SuppressWarnings("All")
public class SlidePageAnim extends HorizonPageAnim {
    private Rect mSrcRect, mDestRect, mNextSrcRect, mNextDestRect;

    public SlidePageAnim(int w, int h, View view, OnPageChangeListener listener) {
        super(w, h, view, listener);
        mSrcRect = new Rect(0, 0, mViewWidth, mViewHeight);
        mDestRect = new Rect(0, 0, mViewWidth, mViewHeight);
        mNextSrcRect = new Rect(0, 0, mViewWidth, mViewHeight);
        mNextDestRect = new Rect(0, 0, mViewWidth, mViewHeight);
    }

    @Override
    public void drawStatic(Canvas canvas) {
        if (isCancel) {
            canvas.drawBitmap(mCurBitmap, 0, 0, null);
        } else {
            canvas.drawBitmap(mNextBitmap, 0, 0, null);
        }
    }

    @Override
    public void drawMove(Canvas canvas) {
        int dis = 0;
        switch (mDirection) {
            case NEXT:
                //左半边的剩余区域
                dis = (int) (mBackgroundWidth - mStartX + mTouchX);
                if (dis > mBackgroundWidth) {
                    dis = mBackgroundWidth;
                }
                //计算bitmap截取的区域
                mSrcRect.left = mBackgroundWidth - dis;
                //计算bitmap在canvas显示的区域
                mDestRect.right = dis;
                //计算下一页截取的区域
                mNextSrcRect.right = mBackgroundWidth - dis;
                //计算下一页在canvas显示的区域
                mNextDestRect.left = dis;

                canvas.drawBitmap(mNextBitmap, mNextSrcRect, mNextDestRect, null);
                canvas.drawBitmap(mCurBitmap, mSrcRect, mDestRect, null);
                break;
            default:
                dis = (int) (mTouchX - mStartX);
                if (dis < 0) {
                    dis = 0;
                    mStartX = mTouchX;
                }
                mSrcRect.left = mBackgroundWidth - dis;
                mDestRect.right = dis;

                //计算下一页截取的区域
                mNextSrcRect.right = mBackgroundWidth - dis;
                //计算下一页在canvas显示的区域
                mNextDestRect.left = dis;

                canvas.drawBitmap(mCurBitmap, mNextSrcRect, mNextDestRect, null);
                canvas.drawBitmap(mNextBitmap, mSrcRect, mDestRect, null);
                break;
        }
    }

    @Override
    public void startAnim() {
        super.startAnim();
        int dx = 0;
        switch (mDirection) {
            case NEXT:
                if (isCancel) {
                    int dis = (int) ((mBackgroundWidth - mStartX) + mTouchX);
                    if (dis > mBackgroundWidth) {
                        dis = mBackgroundWidth;
                    }
                    dx = mBackgroundWidth - dis;
                } else {
                    dx = (int) -(mTouchX + (mBackgroundWidth - mStartX));
                }
                break;
            default:
                if (isCancel) {
                    dx = (int) -Math.abs(mTouchX - mStartX);
                } else {
                    dx = (int) (mBackgroundWidth - (mTouchX - mStartX));
                }
                break;
        }
        //滑动速度保持一致
        int duration = (400 * Math.abs(dx)) / mBackgroundWidth;
        mScroller.startScroll((int) mTouchX, 0, dx, 0, duration);
    }
}
