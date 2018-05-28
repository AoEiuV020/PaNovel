package cc.aoeiuv020.panovel.list

import android.view.View
import cc.aoeiuv020.panovel.data.entity.Novel

/**
 * Created by AoEiuV020 on 2018.05.23-18:03:36.
 */
class NovelMutableListAdapter(
        itemListener: NovelItemActionListener,
        initItem: (View) -> Unit = {}
) : NovelListAdapter(itemListener, initItem) {
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

    fun move(from: Int, to: Int) {
        if (from == to || from !in _data.indices || to !in _data.indices) {
            // 位置不正确就直接返回，
            return
        }
        // ArrayList直接删除插入的话性能不行，但是无所谓了，
        val novel = _data.removeAt(from)
        _data.add(to, novel)
        notifyItemMoved(from, to)
    }
}