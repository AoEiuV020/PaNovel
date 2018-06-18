package cc.aoeiuv020.reader.simple

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cc.aoeiuv020.reader.setHeight
import kotlinx.android.synthetic.main.simple_text_item.view.*
import org.jetbrains.anko.debug
import org.jetbrains.anko.dip

internal class TextViewHolder internal constructor(
        itemView: View,
        private val prAdapter: PageRecyclerAdapter
) : PageRecyclerAdapter.ViewHolder(itemView) {
    private val ctx: Context = itemView.context
    private val divider: View = itemView.divider
    private val textView: TextView = itemView.textView
    fun setText(string: String) {
        divider.setHeight(ctx.dip(prAdapter.mLineSpacing) + ctx.dip(prAdapter.mParagraphSpacing))
        textView.apply {
            prAdapter.debug { "initMargin <${prAdapter.mLeftSpacing}, ${prAdapter.mRightSpacing}>" }
            text = string
            typeface = if (layoutPosition == 0) {
                prAdapter.reader.config.titleFont
            } else {
                prAdapter.reader.config.font
            }
            textSize = prAdapter.mTextSize.toFloat()
            // 直接设置字号的话不会自动调整高度，手动请求一下，
            post {
                requestLayout()
            }
            setTextColor(prAdapter.mTextColor)
            setLineSpacing(context.dip(prAdapter.mLineSpacing).toFloat(), 1.toFloat())
            // 直接设置layoutParams无效，post一下，
            post {
                layoutParams = (layoutParams as ViewGroup.MarginLayoutParams).apply {
                    setMargins((prAdapter.mLeftSpacing.toFloat() / 100 * itemView.width).toInt(),
                            topMargin,
                            (prAdapter.mRightSpacing.toFloat() / 100 * itemView.width).toInt(),
                            bottomMargin)
                }
            }
        }
    }
}