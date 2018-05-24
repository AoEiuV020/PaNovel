package cc.aoeiuv020.panovel.list

import android.support.annotation.LayoutRes
import cc.aoeiuv020.panovel.data.entity.Novel

/**
 * Created by AoEiuV020 on 2018.05.23-18:03:36.
 */
class NovelMutableListAdapter(
        @LayoutRes
        layout: Int,
        itemListener: NovelItemActionListener = NovelItemActionAdapter()
) : NovelListAdapter(layout, itemListener) {
    fun addAll(list: List<Novel>) {
        _data.addAll(list)
        // TODO: 要看看会不要自动滚到底部，不要滚，
        notifyItemRangeInserted(_data.size - list.size, list.size)
    }

    fun clear() {
        _data.clear()
        notifyDataSetChanged()
    }

    fun remove(position: Int) {
        _data.removeAt(position)
        notifyItemRemoved(position)
    }
}