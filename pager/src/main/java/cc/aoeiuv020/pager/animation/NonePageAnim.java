package cc.aoeiuv020.pager.animation;

import android.graphics.Canvas;
import android.view.View;

/**
 * Created by newbiechen on 17-7-24.
 */

@SuppressWarnings("All")
public class NonePageAnim extends HorizonPageAnim {

    public NonePageAnim(AnimationConfig config) {
        super(config);
    }

    public NonePageAnim(int w, int h, Margins margins, View view, OnPageChangeListener listener) {
        super(w, h, margins, view, listener);
    }

    @Override
    public void drawMove(Canvas canvas) {
        if (isCancel) {
            canvas.drawBitmap(mCurBitmap, 0, 0, null);
        } else {
            canvas.drawBitmap(mNextBitmap, 0, 0, null);
        }
    }

    @Override
    public void startAnim() {
    }
}
