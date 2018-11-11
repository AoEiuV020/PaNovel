package cc.aoeiuv020.panovel.settings

import cc.aoeiuv020.panovel.util.Delegates
import cc.aoeiuv020.panovel.util.Pref

/**
 * Created by AoEiuV020 on 2018.11.11-11:52:46.
 */
object DownloadSettings : Pref {
    override val name: String
        get() = "Download"
    /**
     * 下载线程数，
     */
    var downloadThreadsLimit: Int by Delegates.int(4)
    /**
     * 下载线程具体进度的通知，
     */
    var downloadThreadProgress: Boolean by Delegates.boolean(false)
    /**
     * 点击下载时下载的章节数，
     * 0表示下载剩余全部，
     * -1表示每次询问，
     */
    var downloadCount: Int by Delegates.int(-1)
    /**
     * 书架小说刷新章节列表后如果新增章节数小于等于该值就自动缓存新章节，
     */
    var autoDownloadCount: Int by Delegates.int(2)

}