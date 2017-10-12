package cc.aoeiuv020.panovel.text

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelText
import cc.aoeiuv020.panovel.local.Settings
import cc.aoeiuv020.panovel.util.setHeight
import kotlinx.android.synthetic.main.novel_text_item.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.dip

/**
 *
 * Created by AoEiuV020 on 2017.10.12-14:55:56.
 */
class NovelTextRecyclerAdapter(private val ctx: Context) : RecyclerView.Adapter<NovelTextRecyclerAdapter.TextListViewHolder>(), AnkoLogger {
    private var items = emptyList<String>()
    private var mTextSize = Settings.textSize
    private var mLineSpacing = Settings.lineSpacing
    private var mParagraphSpacing = Settings.paragraphSpacing
    private var mTextColor = Settings.textColor

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): TextListViewHolder
            = TextListViewHolder(LayoutInflater.from(ctx).inflate(R.layout.novel_text_item, parent, false))

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TextListViewHolder, position: Int) {
        val item = items[position]
        holder.divider.setHeight(ctx.dip(mParagraphSpacing))
        holder.textView.apply {
            text = "　　" + item
            textSize = mTextSize.toFloat()
            // 直接设置字号的话不会自动调整高度，手动请求一下，
            post {
                requestLayout()
            }
            setTextColor(mTextColor)
            setLineSpacing(ctx.dip(mLineSpacing).toFloat(), 1.toFloat())
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
        items = novelText.textList
        notifyDataSetChanged()
    }

    fun clear() {
        items = emptyList()
        notifyDataSetChanged()
    }

    class TextListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val divider: View = view.divider
        val textView: TextView = view.textView
    }
}
