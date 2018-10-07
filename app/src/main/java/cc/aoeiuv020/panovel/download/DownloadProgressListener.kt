package cc.aoeiuv020.panovel.download

/**
 * Created by AoEiuV020 on 2018.10.07-13:10:45.
 */
interface DownloadProgressListener {
    fun downloading(offset: Long, length: Long)
}