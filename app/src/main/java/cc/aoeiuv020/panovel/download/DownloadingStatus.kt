package cc.aoeiuv020.panovel.download

/**
 * Created by AoEiuV020 on 2018.10.07-15:17:10.
 */
class DownloadingStatus(
        var index: Int = 0,
        var name: String = "",
        var offset: Long = 0,
        var length: Long = 0
) {
    // 进度百分比，
    val progress: Int
        get() = (if (length <= 0) {
            0f
        } else {
            offset.toFloat() / length
        } * 100).toInt()


    fun set(
            index: Int = 0,
            name: String = "",
            offset: Long = 0,
            length: Long = 0
    ) {
        this.index = index
        this.name = name
        this.offset = offset
        this.length = length
    }

}
