package cc.aoeiuv020.panovel.download

import android.support.annotation.MainThread

/**
 * Created by AoEiuV020 on 2018.10.06-19:19:44.
 */
interface DownloadListener {
    @MainThread
    fun downloadStart(count: Int)

    @MainThread
    fun downloading(exists: Int, downloads: Int, errors: Int, left: Int)

    @MainThread
    fun downloadCompletion(exists: Int, downloads: Int, errors: Int)

    /**
     * 这不是下载一个章节失败时调用，
     * 而是线程出现其他意外的异常时调用，
     */
    @MainThread
    fun error(message: String, t: Throwable)
}