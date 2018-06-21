package cc.aoeiuv020.reader.simple

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cc.aoeiuv020.reader.Image
import cc.aoeiuv020.reader.R
import org.jetbrains.anko.AnkoLogger

/**
 *
 * Created by AoEiuV020 on 2017.10.12-14:55:56.
 */
internal class PageRecyclerAdapter(
        val reader: SimpleReader
) : RecyclerView.Adapter<PageRecyclerAdapter.ViewHolder>(), AnkoLogger {
    private val ctx get() = reader.ctx
    private var chapterName: String = ""
    var data: List<String> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    val mTextSize get() = reader.config.textSize
    val mLineSpacing get() = reader.config.lineSpacing
    val mParagraphSpacing get() = reader.config.paragraphSpacing
    val mTextColor get() = reader.config.textColor
    val mLeftSpacing get() = reader.config.contentMargins.left
    val mRightSpacing get() = reader.config.contentMargins.right

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageRecyclerAdapter.ViewHolder {
        return when (ItemType.values()[viewType]) {
            PageRecyclerAdapter.ItemType.TEXT -> {
                val view = LayoutInflater.from(ctx).inflate(R.layout.simple_text_item, parent, false)
                TextViewHolder(view, this)
            }
            PageRecyclerAdapter.ItemType.IMAGE -> {
                val view = LayoutInflater.from(ctx).inflate(R.layout.simple_image_item, parent, false)
                ImageViewHolder(view, this)
            }
        }
    }

    override fun onBindViewHolder(holder: PageRecyclerAdapter.ViewHolder, position: Int) {
        val index = position - 1
        when (holder) {
            is TextViewHolder -> {
                if (position == 0) {
                    holder.setText(chapterName)
                } else {
                    val line = reader.requester.requestParagraph(data[index])
                    holder.setText(line.toString())
                }
            }
            is ImageViewHolder -> {
                val line = reader.requester.requestParagraph(data[index])
                holder.setImage(line as Image)
            }
        }
    }

    /**
     * 数量多出一个展示章节名，
     */
    override fun getItemCount(): Int = data.size + 1

    private enum class ItemType {
        TEXT, IMAGE
    }

    override fun getItemViewType(position: Int): Int {
        // 减去第一个章节名，
        val index = position - 1
        if (index < 0) {
            // 章节名，
            return ItemType.TEXT.ordinal
        }
        val line = reader.requester.requestParagraph(data[index])
        return if (line is Image) {
            // 是图片，
            ItemType.IMAGE
        } else {
            // 是文本，
            ItemType.TEXT
        }.ordinal
    }

    fun clear() {
        data = emptyList()
    }

    fun setChapterName(chapterName: String) {
        this.chapterName = chapterName
        notifyItemChanged(0)
    }

    abstract class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}