package cc.aoeiuv020.panovel.share

import cc.aoeiuv020.panovel.data.entity.Novel
import com.google.gson.annotations.SerializedName

/**
 * 分享书单时供gson解析的bean类，
 * 兼容旧版，
 *
 * Created by AoEiuV020 on 2018.05.28-13:50:33.
 */
class BookListBean(
        @SerializedName("name")
        val name: String,
        @SerializedName("list")
        val list: List<Novel>,
        @SerializedName("version")
        val version: Int
)

class OldBookListBean(
        @SerializedName("name")
        val name: String,
        @SerializedName("list")
        val list: List<OldNovel>
)

class OldNovel(
        @SerializedName("name")
        val name: String,
        @SerializedName("author")
        val author: String,
        @SerializedName("site")
        val site: String,
        @SerializedName("requester")
        val requester: OldRequester
)

class OldRequester(
        @SerializedName("type")
        val type: String,
        @SerializedName("extra")
        val extra: String
)
