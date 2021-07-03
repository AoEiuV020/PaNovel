package cc.aoeiuv020.panovel.settings

import android.content.res.ColorStateList
import cc.aoeiuv020.panovel.share.Expiration
import cc.aoeiuv020.panovel.util.Delegates
import cc.aoeiuv020.panovel.util.Pref

/**
 * Created by AoEiuV020 on 2018.05.26-17:14:31.
 */
object OtherSettings : Pref {
    override val name: String
        get() = "Other"

    /**
     * 搜索时直接刷新章节，
     * 默认关闭是要搜索完成后下拉才会刷新搜索结果的章节，
     */
    var refreshOnSearch: Boolean by Delegates.boolean(false)

    /**
     * 书单分享后网上保存的时限，
     */
    var shareExpiration: Expiration by Delegates.enum(Expiration.NONE)
    var reportCrash: Boolean by Delegates.boolean(true)

    /**
     * 起点数据页打开小说是否要跳到起点app，
     */
    var jumpQidian: Boolean by Delegates.boolean(true)

    var chapterColorDefault: Int by Delegates.int(0xff000000.toInt())

    // TODO: 这个默认颜色改暗些，
    var chapterColorCached: Int by Delegates.int(0xff00ff00.toInt())
    var chapterColorReadAt: Int by Delegates.int(0xffff0000.toInt())
    val chapterColorList
        get() = ColorStateList(
            arrayOf(
                // isChecked代表阅读到的章节，
                intArrayOf(android.R.attr.state_checked),
                // isSelected代表已经缓存的章节，
                intArrayOf(-android.R.attr.state_checked, android.R.attr.state_selected),
                intArrayOf()
            ),
            intArrayOf(
                chapterColorReadAt,
                chapterColorCached,
                chapterColorDefault
            )
        )

    // 起点数据开关，
    var qidianshuju: Boolean by Delegates.boolean(false)

}