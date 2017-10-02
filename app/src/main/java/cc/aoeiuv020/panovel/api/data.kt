@file:Suppress("MemberVisibilityCanPrivate", "unused")

package cc.aoeiuv020.panovel.api

import java.io.Serializable
import java.util.*

/**
 * 相关的数据类定义在这文件里，
 * Created by AoEiuV020 on 2017.10.02-15:27:52.
 */

/**
 * 基类，
 */
open class Data : Serializable

/**
 * 小说网站信息，
 */
data class NovelSite(
        val name: String,
        val baseUrl: String,
        val logo: String,
        val charset: String = "UTF-8"
) : Data()

/**
 * 封装小说详情页地址，
 */
data class NovelDetailUrl(
        val url: String
) : Data()

/**
 * 小说分类页面，
 * 该分类第一页地址，
 */
data class NovelGenre(
        val name: String,
        val url: String,
        val parameters: Map<String, String> = emptyMap()
) : Data()

/**
 * 代表一本小说，由名字和作者唯一决定，
 */
data class NovelItem(
        val name: String,
        val author: String
)

/**
 * 小说列表中的一个小说，
 * @param detailUrl 该小说详情页地址，
 */
data class NovelListItem(
        val novel: NovelItem,
        val detailUrl: NovelDetailUrl,
        // 简介，最新章，或者其他任何有用的信息，
        val info: String = ""
) : Data() {
    constructor(novel: NovelItem, detailUrl: String, info: String = "")
            : this(novel, NovelDetailUrl(detailUrl), info)
}

/**
 * 小说详情页，
 * @param chaptersAsc 升序章节，
 */
data class NovelDetail(
        val novel: NovelItem,
        val bigImg: String,
        // 最后更新的时间，
        val update: Date,
        // 连载 or 完结，
        val status: String,
        // 所属分类，
        val genre: String,
        // 字数，
        val length: String,
        // 简介，
        val info: String,
        // 收藏人数，
        val stars: Int,
        val chaptersAsc: List<NovelChapter>
) : Data()

/**
 * 小说目录，
 * @param url 本章节第一页地址，
 */
data class NovelChapter(
        /**
         * 章节名不包括小说名，
         */
        val name: String,
        val url: String
) : Data()

/**
 * 小说文本，由一个个段落构成，
 */
data class NovelText(
        val textList: List<String>
) : Data()
