@file:Suppress("MemberVisibilityCanPrivate", "unused")

package cc.aoeiuv020.panovel.api

import java.util.*

/**
 * 数据类基类，
 * 相关的数据类定义在这文件里，
 * 不纯粹是数据，
 * Created by AoEiuV020 on 2017.10.02-15:27:52.
 */
abstract class Data

/**
 * 小说网站信息，
 */
data class NovelSite(
        val name: String,
        /**
         * 由于要用来拼接path, 这个[baseUrl]不能斜杆/结尾，
         * http://host
         */
        val baseUrl: String,
        val logo: String,
        /**
         * 这个网站是否启用，
         */
        var enabled: Boolean = true
) : Data()

/**
 * 小说分类页面，
 * 该分类第一页地址，
 */
data class NovelGenre(
        val name: String,
        val requester: Requester
) : Data() {
    constructor(name: String, url: String)
            : this(name, Requester(url))
}

/**
 * 代表一本小说，由网站名，小说名和作者唯一决定，
 * 自带详情页的请求类，
 */
data class NovelItem(
        val site: String,
        val name: String,
        val author: String,
        val requester: Requester
) : Data() {
    constructor(context: NovelContext, name: String, author: String, requester: Requester)
            : this(context.getNovelSite().name, name, author, requester)

    constructor(context: NovelContext, name: String, author: String, url: String)
            : this(context.getNovelSite().name, name, author, Requester(url))

    constructor(site: String, name: String, author: String, url: String)
            : this(site, name, author, Requester(url))
}

/**
 * 小说列表中的一本小说，
 */
data class NovelListItem(
        val novel: NovelItem,
        // 简介，最新章，或者其他任何有用的信息，
        val info: String = ""
) : Data()

/**
 * 小说详情页，
 */
data class NovelDetail(
        val novel: NovelItem,
        val bigImg: String,
        // 最后更新的时间，
        val update: Date,
        // 简介，
        val introduction: String,
        val requester: Requester
) : Data() {
    constructor(novel: NovelItem, bigImg: String, update: Date, info: String, url: String)
            : this(novel, bigImg, update, info, Requester(url))
}

/**
 * 小说目录，
 */
data class NovelChapter(
        /**
         * 章节名不包括小说名，
         */
        val name: String,
        val requester: Requester,
        /**
         * 本章节更新时间，没有就没有，
         */
        val update: Date? = null
) : Data() {
    constructor(name: String, url: String)
            : this(name, Requester(url))

    constructor(name: String, url: String, update: Date)
            : this(name, Requester(url), update)
}

/**
 * 小说文本，由一个个段落构成，
 */
data class NovelText(
        val textList: List<String>
) : Data()

