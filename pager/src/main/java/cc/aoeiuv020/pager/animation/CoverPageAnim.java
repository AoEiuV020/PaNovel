package cc.aoeiuv020.pager.animation;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

/**
 * Created by newbiechen on 17-7-24.
 */

@SuppressWarnings("All")
public class CoverPageAnim extends HorizonPageAnim {

    private Rect mSrcRect, mDestRect;
    private GradientDrawable mBackShadowDrawableLR;

    public CoverPageAnim(AnimationConfig config) {
        super(config);
        init();
    }

    public CoverPageAnim(int w, int h, Margins margins, View view, OnPageChangeListener listener) {
        super(w, h, margins, view, listener);
        init();
    }

    private void init() {
        mSrcRect = new Rect(0, 0, mBackgroundWidth, mBackgroundHeight);
        mDestRect = new Rect(0, 0, mBackgroundWidth, mBackgroundHeight);
        int[] mBackShadowColors = new int[]{0x66000000, 0x00000000};
        mBackShadowDrawableLR = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, mBackShadowColors);
        mBackShadowDrawableLR.setGradientType(GradientDrawable.LINEAR_GRADIENT);
    }

    @Override
    public void drawMove(Canvas canvas) {

        switch (mDirection) {
            case NEXT:
                int dis = (int) (mBackgroundWidth - mStartX + mTouchX);
                if (dis > mBackgroundWidth) {
                    dis = mBackgroundWidth;
                }
                //计算bitmap截取的区域
                mSrcRect.left = mBackgroundWidth - dis;
                //计算bitmap在canvas显示的区域
                mDestRect.right = dis;
                canvas.drawBitmap(mNextBitmap, 0, 0, null);
                canvas.drawBitmap(mCurBitmap, mSrcRect, mDestRect, null);
                addShadow(dis, canvas);
                break;
            default:
                mSrcRect.left = (int) (mBackgroundWidth - mTouchX);
                mDestRect.right = (int) mTouchX;
                canvas.drawBitmap(mCurBitmap, 0, 0, null);
                canvas.drawBitmap(mNextBitmap, mSrcRect, mDestRect, null);
                addShadow((int) mTouchX, canvas);
                break;
        }
    }

    //添加阴影
    public void addShadow(int left, Canvas canvas) {
        mBackShadowDrawableLR.setBounds(left, 0, left + 30, mBackgroundHeight);
        mBackShadowDrawableLR.draw(canvas);
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
                    dx = (int) -mTouchX;
                } else {
                    dx = (int) (mBackgroundWidth - mTouchX);
                }
                break;
        }

        //滑动速度保持一致
        int duration = (getDuration() * Math.abs(dx)) / mBackgroundWidth;
        mScroller.startScroll((int) mTouchX, 0, dx, 0, duration);
    }
}
