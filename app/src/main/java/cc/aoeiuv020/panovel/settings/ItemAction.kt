package cc.aoeiuv020.panovel.settings

import cc.aoeiuv020.panovel.util.Pref
import cc.aoeiuv020.panovel.util.PrefDelegates

/**
 * Created by AoEiuV020 on 2018.05.23-12:51:21.
 */
object ItemAction : Pref {
    override val name: String = "ItemAction"
    var onDotClick: ItemActionEnum by PrefDelegates.enum(ItemActionEnum.Refresh)
    var onDotLongClick: ItemActionEnum by PrefDelegates.enum(ItemActionEnum.Refresh)
    var onNameClick: ItemActionEnum by PrefDelegates.enum(ItemActionEnum.Refresh)
    var onNameLongClick: ItemActionEnum by PrefDelegates.enum(ItemActionEnum.Refresh)
    var onLastChapterClick: ItemActionEnum by PrefDelegates.enum(ItemActionEnum.Refresh)
    var onItemClick: ItemActionEnum by PrefDelegates.enum(ItemActionEnum.Refresh)
    var onItemLongClick: ItemActionEnum by PrefDelegates.enum(ItemActionEnum.Refresh)
}

enum class ItemActionEnum {
    OpenDetail, ReadLastChapter, ReadContinue,
    RefineSearch, Refresh, MoreAction,
    Export, RemoveBookshelf, AddBookshelf,
    // 什么都不做，
    None,
}