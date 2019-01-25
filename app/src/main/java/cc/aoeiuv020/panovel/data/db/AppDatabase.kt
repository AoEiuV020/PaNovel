package cc.aoeiuv020.panovel.data.db

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
        version = 4
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
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    .build().also {
                        sInstance = it
                    }
        }

        private fun migration(start: Int, end: Int, migrate: (SupportSQLiteDatabase) -> Unit) = object : Migration(start, end) {
            override fun migrate(database: SupportSQLiteDatabase) {
                migrate(database)
            }
        }

        @VisibleForTesting
        val MIGRATION_2_3 = migration(2, 3) {
            // Site表添加了置顶时间的字段，
            it.execSQL("ALTER TABLE Site ADD pinnedTime integer default 0 NOT NULL;")
        }

        @VisibleForTesting
        val MIGRATION_3_4 = migration(3, 4) {
            // Site表添加了置顶时间的字段，
            it.execSQL("ALTER TABLE Site ADD hide INTEGER default 0 NOT NULL;")
        }
    }


    abstract fun siteDao(): SiteDao
    abstract fun novelDao(): NovelDao
    abstract fun bookListDao(): BookListDao
}