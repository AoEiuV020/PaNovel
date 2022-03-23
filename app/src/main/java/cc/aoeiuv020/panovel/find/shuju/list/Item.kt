package cc.aoeiuv020.panovel.find.shuju.list

/**
 * Created by AoEiuV020 on 2021.09.06-22:02:55.
 */
data class Item(
    val url: String,
    // 名字可能有省略号需要更新，
    var name: String,
    val author: String,
    val level: String,
    val type: String,
    val words: String,
    val collection: String,
    val firstOrder: String,
    val ratio: String,
    var image: String? = null
)
