package cc.aoeiuv020.panovel.download

/**
 * Created by AoEiuV020 on 2018.10.06-21:33:14.
 */
class DownloadStatus(
        var exists: Int = 0,
        var downloads: Int = 0,
        var errors: Int = 0,
        var left: Int = 0
) {
    // 进度百分比，
    val progress
        get() = ((exists + downloads + errors).toFloat() / ((exists + downloads + errors) + left) * 100).toInt()

    fun set(exists: Int, downloads: Int, errors: Int, left: Int) {
        this.exists = exists
        this.downloads = downloads
        this.errors = errors
        this.left = left
    }

}