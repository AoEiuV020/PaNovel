package cc.aoeiuv020.panovel.data

import android.content.Context
import android.support.annotation.VisibleForTesting
import cc.aoeiuv020.panovel.data.db.AppDatabase

/**
 * 封装一个数据库多个表多个DAO的联用，
 *
 * Created by AoEiuV020 on 2018.04.27-11:52:55.
 */
class AppDatabaseManager(context: Context) {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val db: AppDatabase = AppDatabase.getInstance(context)
}