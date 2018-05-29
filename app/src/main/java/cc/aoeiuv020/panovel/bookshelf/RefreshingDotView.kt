package cc.aoeiuv020.panovel.bookshelf

import android.annotation.TargetApi
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.util.hide
import cc.aoeiuv020.panovel.util.show
import kotlinx.android.synthetic.main.view_refreshing_dot.view.*

/**
 * Created by AoEiuV020 on 2018.05.23-11:48:08.
 */
class RefreshingDotView : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    @TargetApi(21)
    @Suppress("unused")
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        View.inflate(context, R.layout.view_refreshing_dot, this)
    }

    /**
     * 是否显示那个表示有现金操作的三个小点，
     */
    private var showMoreActionIcon: Boolean = false

    fun refreshing() {
        pbRefreshing.show()
        ivDot.hide()
        ivMoreAction.hide()
    }

    fun refreshed(hasNew: Boolean) {
        pbRefreshing.hide()
        if (hasNew) {
            ivDot.show()
        } else if (showMoreActionIcon) {
            ivMoreAction.show()
        }
    }

    fun setDotColor(dotColor: Int) {
        ivDot.setColorFilter(dotColor)
    }
}