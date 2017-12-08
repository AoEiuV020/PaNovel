package cc.aoeiuv020.reader

import cc.aoeiuv020.pager.AnimMode

/**
 * 所有翻页动画，
 * SIMPLE对应旧阅读器，
 * 剩下的对应新阅读器的所有动画，AnimMode,
 * Created by AoEiuV020 on 2017.12.09-01:01:49.
 */
enum class AnimationMode {
    SIMPLE,
    SIMULATION,
    COVER,
    SLIDE,
    NONE,
    SCROLL;

    fun toAnimMode(): AnimMode = when (this) {
        SIMPLE -> {
            throw IllegalStateException("简单翻页动画不可用在这里，")
        }
        SIMULATION -> AnimMode.SIMULATION
        COVER -> AnimMode.COVER
        SLIDE -> AnimMode.SLIDE
        NONE -> AnimMode.NONE
        SCROLL -> AnimMode.SCROLL
    }

    companion object {
        @Suppress("unused")
        fun fromAnimMode(animMode: AnimMode): AnimationMode = when (animMode) {
            AnimMode.SIMULATION -> SIMULATION
            AnimMode.COVER -> COVER
            AnimMode.SLIDE -> SLIDE
            AnimMode.NONE -> NONE
            AnimMode.SCROLL -> SCROLL
        }
    }
}