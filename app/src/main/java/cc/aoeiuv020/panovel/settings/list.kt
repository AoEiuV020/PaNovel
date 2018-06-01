package cc.aoeiuv020.panovel.settings

import cc.aoeiuv020.panovel.util.Delegates
import cc.aoeiuv020.panovel.util.Pref

/**
 * Created by AoEiuV020 on 2018.05.26-17:07:30.
 */
object ListSettings : Pref {
    override val name: String
        get() = "List"
    var gridView: Boolean by Delegates.boolean(false)
    var largeView: Boolean  by Delegates.boolean(true)

    // 书架的小红点设置，
    var dotColor: Int by Delegates.int(0xffff0000.toInt())
    var dotSize: Float by Delegates.float(16f)

    // 点击事件设置，
    var onDotClick: ItemAction by Delegates.enum(ItemAction.Refresh)
    var onDotLongClick: ItemAction by Delegates.enum(ItemAction.Pinned)
    var onCheckUpdateClick: ItemAction by Delegates.enum(ItemAction.Refresh)
    var onNameClick: ItemAction by Delegates.enum(ItemAction.OpenDetail)
    var onNameLongClick: ItemAction by Delegates.enum(ItemAction.RefineSearch)
    var onLastChapterClick: ItemAction by Delegates.enum(ItemAction.ReadLastChapter)
    var onItemClick: ItemAction by Delegates.enum(ItemAction.ReadContinue)
    var onItemLongClick: ItemAction by Delegates.enum(ItemAction.MoreAction)
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