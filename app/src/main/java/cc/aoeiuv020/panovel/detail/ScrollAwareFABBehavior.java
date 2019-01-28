package cc.aoeiuv020.panovel.detail;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;

/**
 * fab消失显示的行为，
 * Created by AoEiuV020 on 2017.09.24-21:29:42.
 */
@SuppressWarnings("ALL")
public class ScrollAwareFABBehavior extends CoordinatorLayout.Behavior<FloatingActionButton> {
    public ScrollAwareFABBehavior(Context context, AttributeSet attrs) {
        super();
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                       @NonNull FloatingActionButton child, @NonNull View directTargetChild, @NonNull View target,
                                       @ViewCompat.ScrollAxis int axes, @ViewCompat.NestedScrollType int type) {
        return true;
    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull final FloatingActionButton child,
                               @NonNull View target, int dxConsumed, int dyConsumed,
                               int dxUnconsumed, int dyUnconsumed, @ViewCompat.NestedScrollType int type) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
        if (dyConsumed > 0 && child.getVisibility() == View.VISIBLE) {
            // 传个回调进去，设为INVISIBLE就不会被跳过了，解决下拉时fab不会重新显示的问题，
            child.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                @Override
                public void onHidden(FloatingActionButton fab) {
                    child.setVisibility(View.INVISIBLE);
                }
            });
        } else if (dyConsumed < 0 && child.getVisibility() != View.VISIBLE) {
            child.show();
        }
    }
}