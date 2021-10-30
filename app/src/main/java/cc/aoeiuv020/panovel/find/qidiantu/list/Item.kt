package cc.aoeiuv020.panovel.find.qidiantu.list

data class Item(
    val url: String,
    // 名字可能有省略号需要更新，
    var name: String,
    val author: String,
    val count: String,
    val type: String,
    val words: String,
    val collection: String,
    val firstOrder: String,
    val ratio: String,
    val dateAdded: String,
    var image: String? = null
)
