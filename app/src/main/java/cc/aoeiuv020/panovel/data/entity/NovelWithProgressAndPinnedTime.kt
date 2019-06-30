package cc.aoeiuv020.panovel.data.entity

import java.util.*

/**
 * Created by AoEiuV020 on 2018.05.31-12:48:03.
 */
data class NovelWithProgressAndPinnedTime(
        /**
         * 网站名，
         * 必须存在，不可空，一本小说至少要有["site", "author“， ”name", "detail"],
         * 不外键到网站表，那张表不稳定，
         */
        var site: String,
        /**
         * 作者名，
         * 必须存在，不可空，一本小说至少要有["site", "author“， ”name", "detail"],
         */
        var author: String,
        /**
         * 小说名，
         * 必须存在，不可空，一本小说至少要有["site", "author“， ”name", "detail"],
         */
        var name: String,
        /**
         * 用于请求小说详情页的额外信息，
         * 必须存在，不可空，一本小说至少要有["site", "author“， ”name", "detail"],
         * [cc.aoeiuv020.panovel.api.NovelItem.extra]
         */
        var detail: String,

        // 阅读进度，

        /**
         * 阅读进度，
         * 阅读至的章节索引，
         */
        var readAtChapterIndex: Int = 0,
        /**
         * 章节内的阅读进度，
         * 看到第几页或者第几个字，具体没决定，
         */
        var readAtTextIndex: Int = 0,
        /**
         * 置顶时间，书架按这个排序，
         * 不置顶的给个最小时间，
         * 不过这样不好判断是否置顶，对比0时间的话，可能有时区问题，
         * 没法设置个按钮置顶时显示取消置顶，
         */
        var pinnedTime: Date = Date(0)
) {
    constructor(novel: Novel)
            : this(novel.site, novel.author, novel.name, novel.detail, novel.readAtChapterIndex, novel.readAtTextIndex, novel.pinnedTime)
}