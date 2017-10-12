package cc.aoeiuv020.panovel.text

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelText
import cc.aoeiuv020.panovel.local.Settings
import cc.aoeiuv020.panovel.util.setHeight
import cn.lemon.view.adapter.BaseViewHolder
import cn.lemon.view.adapter.RecyclerAdapter
import kotlinx.android.synthetic.main.novel_text_item.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.dip

/**
 *
 * Created by AoEiuV020 on 2017.10.12-14:55:56.
 */
class NovelTextRecyclerAdapter(private val ctx: NovelTextActivity) : RecyclerAdapter<String>(ctx), AnkoLogger {
    private var items = emptyList<String>()
    private var mTextSize = Settings.textSize
    private var mLineSpacing = Settings.lineSpacing
    private var mParagraphSpacing = Settings.paragraphSpacing
    private var mTextColor = Settings.textColor

    override fun onCreateBaseViewHolder(parent: ViewGroup?, viewType: Int): BaseViewHolder<String>
            = ViewHolder(parent, R.layout.novel_text_item)

    inner class ViewHolder(parent: ViewGroup?, layoutId: Int) : BaseViewHolder<String>(parent, layoutId) {
        private val divider: View = itemView.divider
        private val textView: TextView = itemView.textView
        @SuppressLint("SetTextI18n")
        override fun setData(data: String) {
            super.setData(data)
            divider.setHeight(context.dip(mParagraphSpacing))
            textView.apply {
                text = "　　" + data
                textSize = mTextSize.toFloat()
                // 直接设置字号的话不会自动调整高度，手动请求一下，
                post {
                    requestLayout()
                }
                setTextColor(mTextColor)
                setLineSpacing(context.dip(mLineSpacing).toFloat(), 1.toFloat())
            }
        }

        override fun onItemViewClick(data: String?) {
            ctx.toggle()
        }
    }

    fun setTextSize(size: Int) {
        debug { "NovelTextListAdapter.setTextSize $size" }
        this.mTextSize = size
        notifyDataSetChanged()
    }

    fun setLineSpacing(size: Int) {
        this.mLineSpacing = size
        notifyDataSetChanged()
    }

    fun setParagraphSpacing(size: Int) {
        this.mParagraphSpacing = size
        notifyDataSetChanged()
    }

    fun setTextColor(color: Int) {
        this.mTextColor = color
        notifyDataSetChanged()
    }

    fun setNovelText(novelText: NovelText) {
        debug { items.size }
        clear()
        addAll(novelText.textList)
        notifyDataSetChanged()
    }
}
