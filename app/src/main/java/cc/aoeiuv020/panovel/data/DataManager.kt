package cc.aoeiuv020.panovel.data

import android.content.Context
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.api.NovelDetail as NovelDetailApi

/**
 * 封装多个数据库的联用，
 * 隐藏api模块的数据类，app只使用这里的数据库实体，
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

    fun listBookshelf(): List<Novel> {
        return app.db.novelDao().listBookshelf()
    }

    fun updateBookshelf(novel: Novel, star: Boolean) {
        app.db.novelDao().updateBookshelf(novel.id, star)
    }
}
