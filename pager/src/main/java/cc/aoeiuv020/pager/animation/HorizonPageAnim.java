package cc.aoeiuv020.pager.animation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by newbiechen on 17-7-24.
 * 横向动画的模板
 */

@SuppressWarnings("All")
public abstract class HorizonPageAnim extends PageAnimation {
    private static final String TAG = "HorizonPageAnim";

    protected Bitmap mCurBitmap;
    protected Bitmap mNextBitmap;
    protected Bitmap mContentBitmap;
    //是否取消翻页
    protected boolean isCancel = false;

    //可以使用 mLast代替
    private int mMoveX = 0;
    private int mMoveY = 0;
    //是否移动了
    private boolean isMove = false;
    //是否翻阅下一页。true表示翻到下一页，false表示上一页。
    private boolean isNext = false;

    //是否没下一页或者上一页
    private boolean noNext = false;

    public HorizonPageAnim(AnimationConfig config) {
        super(config);
        init();
    }

    public HorizonPageAnim(int w, int h, Margins margins, View view, OnPageChangeListener listener) {
        super(w, h, margins, view, listener);
        init();
    }

    private void init() {
        //创建图片
        mCurBitmap = Bitmap.createBitmap(mBackgroundWidth, mBackgroundHeight, Bitmap.Config.RGB_565);
        mNextBitmap = Bitmap.createBitmap(mBackgroundWidth, mBackgroundHeight, Bitmap.Config.RGB_565);
        mContentBitmap = Bitmap.createBitmap(mViewWidth, mViewHeight, Bitmap.Config.ARGB_8888);

        drawCurrent();
    }

    /**
     * 转换页面，在显示下一章的时候，必须首先调用此方法
     */
    public void changePage() {
        Bitmap bitmap = mCurBitmap;
        mCurBitmap = mNextBitmap;
        mNextBitmap = bitmap;
    }

    public void drawStatic(Canvas canvas) {
        canvas.drawBitmap(mNextBitmap, 0, 0, null);
    }

    public abstract void drawMove(Canvas canvas);

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //获取点击位置
        int x = (int) event.getX();
        int y = (int) event.getY();
        //设置触摸点
        setTouchPoint(x, y);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //移动的点击位置
                mMoveX = 0;
                mMoveY = 0;
                //是否移动
                isMove = true;
                //是否存在下一章
                noNext = false;
                //是下一章还是前一章
                isNext = false;
                //是否正在执行动画
                isRunning = false;
                //如果是取消的动画，要切一下页面，
                //如果不存在动画，不知道切不切，但切一下也没事，
                if (isCancel) {
                    changePage();
                }
                //取消
                isCancel = false;
                //设置起始位置的触摸点
                setStartPoint(x, y);
                //如果存在动画则取消动画
                abortAnim();
                break;
            case MotionEvent.ACTION_MOVE:
                //是否存在下一章
                noNext = false;
                //判断是否移动了, 没移动的话在上层处理了，进来的不不可能执行下面一段，
/*
                final int slop = ViewConfiguration.get(mView.getContext()).getScaledTouchSlop();
                if (!isMove) {
                    if (x - mStartX > 0) {
                        // 解决左右来回翻页时仿真动画闪一下的问题，
                        // 问题在哪里并不知道，只是这里设置个方向可以基本解决问题，
                        setDirection(Direction.PRE);
                    } else {
                        setDirection(Direction.NEXT);
                    }
                    isMove = Math.abs(mStartX - x) > slop || Math.abs(mStartY - y) > slop;
                }
*/

                if (isMove) {
                    //判断是否是准备移动的状态(将要移动但是还没有移动)
                    if (mMoveX == 0 && mMoveY == 0) {
                        //判断翻得是上一页还是下一页
                        if (x - mStartX > 0) {
                            //上一页的参数配置
                            isNext = false;
                            setDirection(Direction.PRE);
                            // 重设touch点，
                            setTouchPoint(x, y);
                            boolean hasPrev = drawPrev();
                            //如果上一页不存在
                            if (!hasPrev) {
                                noNext = true;
                                return true;
                            }
                        } else {
                            //进行下一页的配置
                            isNext = true;
                            //判断是否下一页存在
                            setDirection(Direction.NEXT);
                            // 重设touch点，由于受方向影响，改变方向后必须重新设置touch点，
                            setTouchPoint(x, y);
                            boolean hasNext = drawNext();
                            //如果存在设置动画方向

                            //如果不存在表示没有下一页了
                            if (!hasNext) {
                                noNext = true;
                                return true;
                            }
                        }
                    } else {
                        //判断是否取消翻页
                        if (isNext) {
                            if (x - mMoveX > 0) {
                                isCancel = true;
                            } else {
                                isCancel = false;
                            }
                        } else {
                            if (x - mMoveX < 0) {
                                isCancel = true;
                            } else {
                                isCancel = false;
                            }
                        }
                    }

                    mMoveX = x;
                    mMoveY = y;
                    isRunning = true;
                    mView.invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!isMove) {
                    if (x < mBackgroundWidth / 2) {
                        isNext = false;
                    } else {
                        isNext = true;
                    }

                    if (isNext) {
                        if (!scrollNext(x, y)) return true;
                    } else {
                        if (!scrollPrev(x, y)) return true;
                    }
                } else {
                    // 是否取消翻页
                    if (isCancel) {
                        pageCancel();
                    }

                    scroll();
                }

                break;
        }
        return true;
    }

    /**
     * 开始滚动，
     */
    private void scroll() {
        // 开启翻页效果
        if (!noNext) {
            startAnim();
            mView.invalidate();
        }
    }

    /**
     * @param x 翻页动画起始x，
     * @param y 翻页动画起始y，
     * @return 返回是否翻页，也就是是否有下一页，
     */
    @Override
    public boolean scrollNext(float x, float y) {
        // 动画起始，
        setTouchPoint(x, y);
        //判断是否下一页存在
        boolean hasNext = drawNext();
        //设置动画方向
        setDirection(Direction.NEXT);
        if (!hasNext) {
            return false;
        }
        scroll();
        return true;
    }

    @Override
    public boolean scrollPrev(float x, float y) {
        setTouchPoint(x, y);
        boolean hasPrev = drawPrev();
        setDirection(Direction.PRE);
        if (!hasPrev) {
            return false;
        }
        scroll();
        return true;
    }

    @Override
    public void draw(Canvas canvas) {
        if (isRunning) {
            drawMove(canvas);
        } else {
            if (isCancel) {
                mNextBitmap = mCurBitmap.copy(Bitmap.Config.RGB_565, true);
            }
            drawStatic(canvas);
        }
    }

    @Override
    public void scrollAnim() {
        if (mScroller.computeScrollOffset()) {
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();

            setTouchPoint(x, y);

            if (mScroller.getFinalX() == x && mScroller.getFinalY() == y) {
                isRunning = false;
                if (isCancel) {
                    drawCurrent();
                }
            }
            mView.postInvalidate();
        }
    }

    @Override
    public void abortAnim() {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
            isRunning = false;
            if (isCancel) {
                drawCurrent();
            }
            mView.postInvalidate();
        }
    }

    public Bitmap getBgBitmap() {
        return mNextBitmap;
    }

    public Bitmap getContent() {
        return mContentBitmap;
    }

    @Override
    public Canvas getBgCanvas() {
        return new Canvas(getBgBitmap());
    }

    @Override
    public Canvas getConentCanvas() {
        return new Canvas(getContent());
    }

    public void copyContent(Canvas canvas) {
        canvas.drawBitmap(mContentBitmap, mMarginWidth, mMarginHeight, null);
    }
}
