package cc.aoeiuv020.pager.animation

import android.view.View

/**
 *
 * Created by AoEiuV020 on 2017.12.09-15:42:38.
 */
data class AnimationConfig(
        var width: Int,
        var height: Int,
        var margins: Margins,
        var view: View,
        var listener: PageAnimation.OnPageChangeListener,
        var durationMultiply: Float
)