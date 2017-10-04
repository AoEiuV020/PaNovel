@file:Suppress("MemberVisibilityCanPrivate", "unused")

package cc.aoeiuv020.panovel.api

import java.io.Serializable
import java.util.*

/**
 * 数据类基类，
 * 相关的数据类定义在这文件里，
 * 不纯粹是数据，
 * Created by AoEiuV020 on 2017.10.02-15:27:52.
 */
abstract class Data : Serializable

/**
 * 小说网站信息，
 */
data class NovelSite(
        val name: String,
        val baseUrl: String,
        val logo: String
) : Data()

/**
 * 小说分类页面，
 * 该分类第一页地址，
 */
data class NovelGenre(
        val name: String,
        val requester: ListRequester
) : Data() {
    constructor(name: String, url: String)
            : this(name, ListRequester(url))
}

/**
 * 代表一本小说，由小说名和作者唯一决定，
 */
data class NovelItem(
        val name: String,
        val author: String
) : Data()

/**
 * 小说列表中的一本小说，
 */
data class NovelListItem(
        val novel: NovelItem,
        val requester: DetailRequester,
        // 简介，最新章，或者其他任何有用的信息，
        val info: String = ""
) : Data() {
    constructor(novel: NovelItem, detailUrl: String, info: String = "")
            : this(novel, DetailRequester(detailUrl), info)
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
 */
data class NovelChapter(
        /**
         * 章节名不包括小说名，
         */
        val name: String,
        val requester: TextRequester
) : Data() {
    constructor(name: String, url: String)
            : this(name, TextRequester(url))
}

/**
 * 小说文本，由一个个段落构成，
 */
data class NovelText(
        val textList: List<String>
) : Data()

