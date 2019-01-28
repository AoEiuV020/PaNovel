package cc.aoeiuv020.panovel.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.util.noCover
import cc.aoeiuv020.panovel.util.notNullOrReport
import java.util.*

/**
 * 一本小说由“网站名-作者名-小说名”共同唯一确定，
 * 通过网站名找到网站上下文类，
 * 要有extra额外的信息才能请求到小说详情页，
 *
 * Created by AoEiuV020 on 2018.05.22-09:14:02.
 */
@Entity(
        indices = [
            (Index(
                    value = ["site", "author", "name"],
                    unique = true
            )),
            (Index(
                    value = ["bookshelf"],
                    unique = false
            ))
        ]
)
@Suppress("MemberVisibilityCanBePrivate")
data class Novel(
        /**
         * 普通的id,
         * 要给个null才能autoGenerate，
         * 插入时拿到id再赋值回来，所以要可变var，
         */
        @PrimaryKey(autoGenerate = true)
        var id: Long? = null,
        /**
         * 网站名，
         * 必须存在，不可空，一本小说至少要有["site", "author“， ”name", "detail"],
         * 不外键到网站表，那张表不稳定，
         *
         * 点.开头的表示本地小说，带上格式，比如".txt"".epub",
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

        // 下面的有默认值，每次阅读后改变，

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
         * 是否在书架上，sqlite没有分区，只建个索引，
         */
        var bookshelf: Boolean = false,
        /**
         * 章节数，
         */
        var chaptersCount: Int = 0,

        // 小说详情页的信息，一次更新后写死，
        // 之后就非空，
        /**
         * 图片地址，没有图片的统一填充一张写着没有封面的图片地址，不可空，
         * 默认一样统一填充一张写着没有封面的图片地址，不空，
         * 然后展示时判断，是noCover就填充内置的暂无封面的封面，
         * 直接改成可空更好，但是sqlite不能改字段，
         */
        var image: String = noCover,
        /**
         * 简介，获取后如果小说没有简介，留空白或者字符串null，不可空，
         */
        var introduction: String = VALUE_NULL,
        /**
         * 用于请求小说章节列表的extra, 获取小说详情后不可空，
         * 不能给默认值，要留着判断是否需要请求小说详情页，
         * [cc.aoeiuv020.panovel.api.NovelDetail.extra]
         *
         * 对于本地线文本小说就是编码，
         */
        var chapters: String? = null,

        // 下面的本方该可空，强行给个初值，
        // 有机会就更新，
        /**
         * 最新章节名, 刷新章节列表时更新，
         * 这个章节名只用于展示，所以可以给个默认空字符串，
         */
        var lastChapterName: String = VALUE_NULL,
        /**
         * 阅读进度章节名, 阅读后更新，
         * 这个章节名只用于展示，所以可以给个默认空字符串，
         */
        var readAtChapterName: String = VALUE_NULL,
        /**
         * 上次阅读时间，在阅读后更新，
         * 时间只用于对比和展示，没阅读过就是默认最小时间，
         */
        var readTime: Date = Date(0),
        /**
         * 最新更新时间, 也就是最新一章更新的时间，刷新章节列表如果存在这个时间就更新，
         * 可能不打算用这个字段，判断是否更新应该用[receiveUpdateTime]比较靠谱，
         * 刷新详情或者章节可能并不返回更新时间，可能只有null, 非空就覆盖这个字段，
         * 时间只用于对比和展示，没刷新过章节就是默认最小时间，
         */
        var updateTime: Date = Date(0),
        /**
         * 检查更新时间, 也就是这个时间之前的更新是已知的，不论有无更新，
         * 时间只用于对比和展示，没刷新过章节就是默认最小时间，
         */
        var checkUpdateTime: Date = Date(0),
        /**
         * 拿到上一个更新的时间, 也就是上次刷出更新的[checkUpdateTime],
         * 是否刷出更新只判断章节数量，
         *
         * 用来对比阅读时间就知道是否是已读了，
         * 时间只用于对比和展示，没刷新过章节就是默认最小时间，
         */
        var receiveUpdateTime: Date = Date(0),
        /**
         * 置顶时间，书架按这个排序，
         * 不置顶的给个最小时间，
         * 不过这样不好判断是否置顶，对比0时间的话，可能有时区问题，
         * 没法设置个按钮置顶时显示取消置顶，
         */
        var pinnedTime: Date = Date(0)
) {
    // id的非空版本，实在是要经常用id, 而且是不可能为空的id,
    val nId: Long get() = id.notNullOrReport()
    // chapters的非空版本，用的不多，
    val nChapters: String get() = chapters.notNullOrReport()

    /**
     * 打印日志经常要用，这三者决定一本小说，
     */
    val bookId: String get() = "$name.$author.${site.removePrefix(".")}"

    val isLocalNovel: Boolean get() = site.startsWith(".")

    fun readAt(index: Int, chapters: List<NovelChapter>) {
        readAtChapterIndex = index
        if (index in chapters.indices) {
            // 仅限于章节数正常的情况，否则可能chapters为空list导致崩溃，
            // 同时改保存的章节名，
            readAtChapterName = chapters[index].name
        }
    }

    companion object {
        // 用作传参时的key,
        const val KEY_ID: String = "id"
        const val VALUE_NULL: String = "(null)"
    }
}
