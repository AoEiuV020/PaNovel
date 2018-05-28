package cc.aoeiuv020.panovel.settings

import cc.aoeiuv020.panovel.util.Delegates
import cc.aoeiuv020.panovel.util.Pref

/**
 * Created by AoEiuV020 on 2018.05.26-17:07:30.
 */
object ListSettings : Pref {
    override val name: String
        get() = "List"
    val dotColor: Int by Delegates.int(0xffff0000.toInt())
    val dotSize: Float by Delegates.float(24f)

}

object ItemActionSettings : Pref {
    override val name: String = "ClickAction"
    val onDotClick: ItemActionEnum by Delegates.enum(ItemActionEnum.Refresh)
    val onDotLongClick: ItemActionEnum by Delegates.enum(ItemActionEnum.Pinned)
    val onCheckUpdateClick: ItemActionEnum by Delegates.enum(ItemActionEnum.Refresh)
    val onNameClick: ItemActionEnum by Delegates.enum(ItemActionEnum.OpenDetail)
    val onNameLongClick: ItemActionEnum by Delegates.enum(ItemActionEnum.RefineSearch)
    val onLastChapterClick: ItemActionEnum by Delegates.enum(ItemActionEnum.ReadLastChapter)
    val onItemClick: ItemActionEnum by Delegates.enum(ItemActionEnum.ReadContinue)
    val onItemLongClick: ItemActionEnum by Delegates.enum(ItemActionEnum.MoreAction)
}

enum class ItemActionEnum {
    OpenDetail, ReadLastChapter, ReadContinue,
    RefineSearch, Refresh, MoreAction,
    Export, RemoveBookshelf, AddBookshelf,
    // 置顶，
    Pinned,
    CancelPinned,
    // 什么都不做，
    None,
}