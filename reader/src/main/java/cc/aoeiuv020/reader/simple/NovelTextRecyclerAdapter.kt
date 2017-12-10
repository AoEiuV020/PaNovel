package cc.aoeiuv020.reader.simple

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cc.aoeiuv020.reader.R
import cc.aoeiuv020.reader.setHeight
import kotlinx.android.synthetic.main.simple_text_item.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.dip

/**
 *
 * Created by AoEiuV020 on 2017.10.12-14:55:56.
 */
internal class NovelTextRecyclerAdapter(private val reader: SimpleReader) : RecyclerView.Adapter<NovelTextRecyclerAdapter.ViewHolder>(), AnkoLogger {
    private val ctx get() = reader.ctx
    private var chapterName: String = ""
    var data: List<String> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    private val mTextSize get() = reader.config.textSize
    private val mLineSpacing get() = reader.config.lineSpacing
    private val mParagraphSpacing get() = reader.config.paragraphSpacing
    private val mTextColor get() = reader.config.textColor
    private val mLeftSpacing get() = reader.config.leftSpacing
    private val mRightSpacing get() = reader.config.rightSpacing

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(ctx).inflate(R.layout.simple_text_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == 0) {
            holder.setChapterName(chapterName)
        } else {
            holder.setNovelText(data[position - 1])
        }
    }

    /**
     * 数量多出一个展示章节名，
     */
    override fun getItemCount(): Int = data.size + 1

    fun clear() {
        data = emptyList()
    }

    fun setChapterName(chapterName: String) {
        this.chapterName = chapterName
        notifyItemChanged(0)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val divider: View = itemView.divider
        private val textView: TextView = itemView.textView
        private fun setText(string: String) {
            divider.setHeight(ctx.dip(mLineSpacing) + ctx.dip(mParagraphSpacing))
            textView.apply {
                debug { "initMargin <$mLeftSpacing, $mRightSpacing>" }
                text = string
                typeface = reader.config.font
                textSize = mTextSize.toFloat()
                // 直接设置字号的话不会自动调整高度，手动请求一下，
                post {
                    requestLayout()
                }
                setTextColor(mTextColor)
                setLineSpacing(context.dip(mLineSpacing).toFloat(), 1.toFloat())
                // 直接设置layoutParams无效，post一下，
                post {
                    layoutParams = (layoutParams as ViewGroup.MarginLayoutParams).apply {
                        setMargins((mLeftSpacing.toFloat() / 100 * itemView.width).toInt(),
                                topMargin,
                                (mRightSpacing.toFloat() / 100 * itemView.width).toInt(),
                                bottomMargin)
                    }
                }
            }
        }

        fun setChapterName(chapterName: String) {
            setText(chapterName)
        }

        fun setNovelText(text: String) {
            setText("　　" + text)
        }

    }
}