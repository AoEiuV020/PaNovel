package cc.aoeiuv020.pager.animation

import android.view.View
import cc.aoeiuv020.pager.IMargins

/**
 *
 * Created by AoEiuV020 on 2017.12.09-15:42:38.
 */
data class AnimationConfig(
        var width: Int,
        var height: Int,
        var margins: IMargins,
        var view: View,
        var listener: PageAnimation.OnPageChangeListener,
        var durationMultiply: Float
)