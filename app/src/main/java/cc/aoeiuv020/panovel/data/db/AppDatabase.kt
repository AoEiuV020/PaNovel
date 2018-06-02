package cc.aoeiuv020.panovel.data.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context
import cc.aoeiuv020.panovel.data.dao.BookListDao
import cc.aoeiuv020.panovel.data.dao.NovelDao
import cc.aoeiuv020.panovel.data.dao.SiteDao
import cc.aoeiuv020.panovel.data.entity.BookList
import cc.aoeiuv020.panovel.data.entity.BookListItem
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.data.entity.Site

/**
 * Created by AoEiuV020 on 2018.05.13-18:00:33.
 */
@Database(
        entities = [Novel::class, Site::class, BookListItem::class, BookList::class],
        version = 2
)
@TypeConverters(value = [DateTypeConverter::class])
abstract class AppDatabase : RoomDatabase() {
    companion object {
        private var sInstance: AppDatabase? = null
        @Synchronized
        fun getInstance(context: Context): AppDatabase {
            val dbFile = context.getDatabasePath("PaNovel-app.db")
            return sInstance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    dbFile.path)
                    // 版本1的数据库只有网站启用设置，不迁移，略麻烦，
                    .fallbackToDestructiveMigrationFrom(1)
                    .build().also {
                        sInstance = it
                    }
        }
    }

    abstract fun siteDao(): SiteDao
    abstract fun novelDao(): NovelDao
    abstract fun bookListDao(): BookListDao
}