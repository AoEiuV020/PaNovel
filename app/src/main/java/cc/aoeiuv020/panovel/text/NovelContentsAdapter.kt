package cc.aoeiuv020.panovel.text

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.settings.OtherSettings
import kotlinx.android.synthetic.main.novel_chapter_item.view.*

/**
 *
 * Created by AoEiuV020 on 2017.10.22-17:26:00.
 */
class NovelContentsAdapter(
        val context: Context,
        val novel: Novel,
        val chapters: List<NovelChapter>,
        // 只用contains方法判断章节是否已经缓存，
        private var cachedList: Collection<String>
) : BaseAdapter() {
    // 颜色列表，只读一次设置，
    private val chapterColorList = OtherSettings.chapterColorList

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView
                ?: LayoutInflater.from(context).inflate(R.layout.novel_chapter_item, parent, false).apply {
                    name.setTextColor(chapterColorList)
                }
        val nameTextView = view.name
        val chapter = getItem(position)
        nameTextView.apply {
            text = chapter.name
            // isChecked代表阅读到的章节，
            isChecked = novel.readAtChapterIndex == position
            // isSelected代表已经缓存的章节，
            isSelected = cachedList.contains(chapter.extra)
        }
        return view
    }

    override fun getItem(position: Int): NovelChapter = chapters[position]

    override fun getItemId(position: Int): Long = 0L

    override fun getCount(): Int = chapters.size
}