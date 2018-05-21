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
 * 代表一本小说，由网站名，小说名和作者唯一决定，
 *
 * @param extra 并不重要了已经，只要网站Context能用这个请求到详情页就可以，同一本小说不同extra也可以，
 */
data class NovelItem(
        val site: String,
        val name: String,
        val author: String,
        val extra: String
) : Data() {
    constructor(context: NovelContext, name: String, author: String, extra: String)
            : this(context.getNovelSite().name, name, author, extra)
}

/**
 * 小说详情页，
 * @param extra 用于请求章节列表，
 */
data class NovelDetail(
        val novel: NovelItem,
        // TODO: 考虑改名，就叫image,
        val bigImg: String,
        // 最后更新的时间，
        val update: Date?,
        // 简介，
        val introduction: String,
        val extra: String
) : Data()

/**
 * 小说目录，
 * @param extra 用于请求本章节正文，
 */
data class NovelChapter(
        /**
         * 章节名不包括小说名，
         */
        val name: String,
        val extra: String,
        /**
         * 本章节更新时间，没有就没有，
         * 为了可以只修改列表中最后一章的更新时间，这个变量要可变，
         * 因为有的网站目录页和详情页是一样的，可以拿到最后更新时间，
         */
        var update: Date? = null
) : Data()

/**
 * 小说正文，由一个个段落构成，
 * TODO: 为了支持图片，要把String改成别的，比如直接Any, 类名也可以改掉，
 */
data class NovelText(
        val textList: List<String>
) : Data()

