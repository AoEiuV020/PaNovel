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
    var largeView: Boolean by Delegates.boolean(true)
    var pinnedBackgroundColor: Int by Delegates.int(0xffefefef.toInt())

    // 书架的小红点设置，
    var dotColor: Int by Delegates.int(0xffff0000.toInt())
    var dotSize: Float by Delegates.float(16f)

    /**
     * 小红点提示有更新，
     * 是则提示刷出更新的时间在阅读时间之后，
     * 否则提示阅读进度没到最新章节，
     */
    var dotNotifyUpdate: Boolean by Delegates.boolean(true)

    // 点击事件设置，
    var onDotClick: ItemAction by Delegates.enum(ItemAction.Refresh)
    var onDotLongClick: ItemAction by Delegates.enum(ItemAction.Pinned)
    var onCheckUpdateClick: ItemAction by Delegates.enum(ItemAction.Refresh)
    var onNameClick: ItemAction by Delegates.enum(ItemAction.OpenDetail)
    var onNameLongClick: ItemAction by Delegates.enum(ItemAction.RefineSearch)
    var onLastChapterClick: ItemAction by Delegates.enum(ItemAction.ReadLastChapter)
    var onItemClick: ItemAction by Delegates.enum(ItemAction.ReadContinue)
    var onItemLongClick: ItemAction by Delegates.enum(ItemAction.MoreAction)

    /**
     * 书架的排序标准，
     * 置顶的固定置顶，
     */
    var bookshelfOrderBy: OrderBy by Delegates.enum(OrderBy.Smart)
}

enum class ItemAction {
    OpenDetail, ReadLastChapter, ReadContinue,
    RefineSearch, Refresh, MoreAction,
    Export, RemoveBookshelf, AddBookshelf,
    Cache,
    // 置顶，
    Pinned,
    CancelPinned,
    // 删除缓存，删除所有相关数据，
    CleanCache,
    CleanData,
    // 什么都不做，
    None,
}

enum class OrderBy {
    Id, ReadTime, UpdateTime,
    // 综合阅读时间和更新时间，
    Smart,
    Name, Author, Site,
}