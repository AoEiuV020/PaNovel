package cc.aoeiuv020.panovel.text

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelText
import cc.aoeiuv020.panovel.local.Settings
import kotlinx.android.synthetic.main.novel_text_item.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.dip
import java.util.RandomAccess
import kotlin.collections.ArrayList

class NovelTextListAdapter(private val ctx: Context) : BaseAdapter(), AnkoLogger {
    private var items = emptyList<String>()
    private var textSize = Settings.textSize
    private var lineSpacing = Settings.lineSpacing
    private var textColor = Settings.textColor

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View
            = (convertView ?: LayoutInflater.from(ctx).inflate(R.layout.novel_text_item, parent, false)).apply {
        textView.text = "　　" + getItem(position)
        textView.textSize = textSize.toFloat()
        // 直接设置字号的话不会自动调整高度，手动请求一下，
        textView.post {
            textView.requestLayout()
        }
        textView.setTextColor(textColor)
        textView.setLineSpacing(ctx.dip(lineSpacing).toFloat(), 1.toFloat())
    }

    override fun getItem(position: Int) = items[position]

    override fun getItemId(position: Int) = 0L

    override fun getCount() = items.size

    fun setTextSize(size: Int) {
        debug { "NovelTextListAdapter.setTextSize $size" }
        this.textSize = size
        notifyDataSetChanged()
    }

    fun setLineSpacing(size: Int) {
        this.lineSpacing = size
        notifyDataSetChanged()
    }

    fun setTextColor(color: Int) {
        this.textColor = color
        notifyDataSetChanged()
    }

    fun setNovelText(novelText: NovelText) {
        debug { items.size }
        items = novelText.textList.let { if (it is RandomAccess) it else ArrayList(it) }
        notifyDataSetChanged()
    }

    override fun isEnabled(position: Int): Boolean = false
}