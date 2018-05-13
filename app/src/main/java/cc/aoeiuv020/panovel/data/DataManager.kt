package cc.aoeiuv020.panovel.data

import android.content.Context
import cc.aoeiuv020.panovel.api.NovelDetail as NovelDetailApi

/**
 * 封装多个数据库的联用，
 * 隐藏所有数据库实体，这里进出的都是专用的kotlin数据类，
 *
 * Created by AoEiuV020 on 2018.04.28-16:53:14.
 */
object DataManager {
    lateinit var app: AppDatabaseManager
    @Synchronized
    fun init(context: Context) {
        if (!::app.isInitialized) {
            app = AppDatabaseManager(context)
        }
    }
}
