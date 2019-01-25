package cc.aoeiuv020.reader.simple

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView

/**
 * https://stackoverflow.com/a/12283909/5615186
 * Created by AoEiuV020 on 2018.06.11-10:56:37.
 */
class ResizableImageView : AppCompatImageView {
    constructor(context: Context)
            : super(context)

    constructor(context: Context, attrs: AttributeSet)
            : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val d = drawable

        if (d != null) {
            // ceil not round - avoid thin vertical gaps along the left/right edges
            val width = View.MeasureSpec.getSize(widthMeasureSpec)
            val height = Math.ceil((width.toFloat() * d.intrinsicHeight.toFloat() / d.intrinsicWidth.toFloat()).toDouble()).toInt()
            setMeasuredDimension(width, height)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

}