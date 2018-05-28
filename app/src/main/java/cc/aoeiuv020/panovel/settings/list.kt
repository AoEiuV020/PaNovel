package cc.aoeiuv020.panovel.settings

import cc.aoeiuv020.panovel.util.Delegates
import cc.aoeiuv020.panovel.util.Pref

/**
 * Created by AoEiuV020 on 2018.05.26-17:07:30.
 */
object ListSettings : Pref {
    override val name: String
        get() = "List"
    val gridView: Boolean by Delegates.boolean(false)
    val largeView: Boolean  by Delegates.boolean(true)

    // 书架的小红点设置，
    val dotColor: Int by Delegates.int(0xffff0000.toInt())
    val dotSize: Float by Delegates.float(24f)

    // 点击事件设置，
    val onDotClick: ItemAction by Delegates.enum(ItemAction.Refresh)
    val onDotLongClick: ItemAction by Delegates.enum(ItemAction.Pinned)
    val onCheckUpdateClick: ItemAction by Delegates.enum(ItemAction.Refresh)
    val onNameClick: ItemAction by Delegates.enum(ItemAction.OpenDetail)
    val onNameLongClick: ItemAction by Delegates.enum(ItemAction.RefineSearch)
    val onLastChapterClick: ItemAction by Delegates.enum(ItemAction.ReadLastChapter)
    val onItemClick: ItemAction by Delegates.enum(ItemAction.ReadContinue)
    val onItemLongClick: ItemAction by Delegates.enum(ItemAction.MoreAction)
}

enum class ItemAction {
    OpenDetail, ReadLastChapter, ReadContinue,
    RefineSearch, Refresh, MoreAction,
    Export, RemoveBookshelf, AddBookshelf,
    // 置顶，
    Pinned,
    CancelPinned,
    // 什么都不做，
    None,
}