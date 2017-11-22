package cc.aoeiuv020.panovel.base.item

import cc.aoeiuv020.panovel.api.NovelItem

/**
 *
 * Created by AoEiuV020 on 2017.11.22-17:01:30.
 */
interface OnItemLongClickListener {
    fun onItemLongClick(position: Int, novelItem: NovelItem)
}