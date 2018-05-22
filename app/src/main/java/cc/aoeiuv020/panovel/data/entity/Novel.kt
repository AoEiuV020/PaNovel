package cc.aoeiuv020.panovel.data.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
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
                    value = ["site", "author“， ”name"],
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
         */
        @PrimaryKey(autoGenerate = true)
        val id: Long? = null,
        /**
         * 小说名，
         */
        val name: String,
        /**
         * 作者名，
         */
        val author: String,
        /**
         * 网站名，
         */
        val site: String,
        /**
         * 用于请求小说详情页的额外信息，
         * [cc.aoeiuv020.panovel.api.NovelItem.extra]
         */
        val detail: String,

        // 下面的有默认值，每次阅读后改变，

        /**
         * 阅读进度，
         * 阅读至的章节索引，
         */
        val readAtChapterIndex: Int = 0,
        /**
         * 章节内的阅读进度，
         * 看到第几页或者第几个字，具体没决定，
         */
        val readAtTextIndex: Int = 0,
        /**
         * 是否在书架上，sqlite没有分区，只建个索引，
         */
        val bookshelf: Boolean = false,
        /**
         * 章节数，
         */
        val chaptersCount: Int = 0,

        // 小说详情页的信息，一次更新后写死，之后就非空，
        /**
         * 图片地址，没有图片的统一填充一张写着没有封面的图片地址，不可空，
         */
        val image: String? = null,
        /**
         * 简介，获取后如果小说没有简介，留空白或者字符串null，不可空，
         */
        val introduction: String? = null,
        /**
         * 用于请求小说章节列表的extra, 获取小说详情后不可空，
         * [cc.aoeiuv020.panovel.api.NovelDetail.extra]
         */
        val chapters: String? = null,

        // 下面的可空，有机会就更新，
        /**
         * 最新章节名, 刷新章节列表时更新，
         */
        val lastChapterName: String? = null,
        /**
         * 阅读进度章节名, 阅读后更新，
         */
        val readAtChapterName: String? = null,
        /**
         * 上次阅读时间，在后更新，
         */
        val readTime: Date? = null,
        /**
         * 最新更新时间, 也就是最新一章更新的时间，刷新章节列表如果存在这个时间就更新，
         * 可能不打算用这个字段，判断是否更新应该用[receiveUpdateTime]比较靠谱，
         */
        val updateTime: Date? = null,
        /**
         * 检查更新时间, 也就是这个时间之前的更新是已知的，不论有无更新，
         */
        val checkUpdateTime: Date? = null,
        /**
         * 拿到上一个更新的时间, 也就是上次刷出更新的[checkUpdateTime],
         * 用来对比阅读时间就知道是否是已读了，
         */
        val receiveUpdateTime: Date? = null
)