package cc.aoeiuv020.panovel.data.db

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cc.aoeiuv020.panovel.data.entity.Site
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

/**
 * Created by AoEiuV020 on 2018.06.10-19:56:00.
 */
@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {
    @Rule
    @JvmField
    val helper: MigrationTestHelper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(),
            AppDatabase::class.java.canonicalName,
            FrameworkSQLiteOpenHelperFactory())

    @Test
    fun migration23Test() {
        val site = Site("飘天文学", "https://www.piaotian.com", "https://www.piaotian.com/css/logo.gif", true, Date(0))
        val db2 = helper.createDatabase(TEST_DB, 2)
        db2.execSQL("INSERT INTO Site (name, baseUrl, logo, enabled) VALUES ('${site.name}', '${site.baseUrl}', '${site.logo}', ${if (site.enabled) 1 else 0});")
        db2.close()
        // 如果迁移后的数据库信息不匹配，这句就会报错，
        val db3 = helper.runMigrationsAndValidate(TEST_DB, 3, true, AppDatabase.MIGRATION_2_3)
        val c = db3.query("select * from Site;")
        println(c.columnNames.joinToString())
        assertEquals(1, c.count)
        if (c.moveToNext()) {
            val name = c.getString(0)
            val baseUrl = c.getString(1)
            val logo = c.getString(2)
            val enabled = c.getInt(3) > 0
            val pinnedTime = Date(c.getLong(4))
            val result = Site(name, baseUrl, logo, enabled, pinnedTime)
            println(result)
            assertEquals(site, result)
        }
    }

    @Test
    fun migration34Test() {
        val site = Site("飘天文学", "https://www.piaotian.com", "https://www.piaotian.com/css/logo.gif", true, Date(0), false)
        val db2 = helper.createDatabase(TEST_DB, 3)
        db2.execSQL("INSERT INTO Site (name, baseUrl, logo, enabled, pinnedTime) VALUES ('${site.name}', '${site.baseUrl}', '${site.logo}', ${if (site.enabled) 1 else 0}, ${site.pinnedTime.time});")
        db2.close()
        // 如果迁移后的数据库信息不匹配，这句就会报错，
        val db3 = helper.runMigrationsAndValidate(TEST_DB, 4, true, AppDatabase.MIGRATION_3_4)
        val c = db3.query("select * from Site;")
        println(c.columnNames.joinToString())
        assertEquals(1, c.count)
        c.moveToNext()
        val name = c.getString(0)
        val baseUrl = c.getString(1)
        val logo = c.getString(2)
        val enabled = c.getInt(3) > 0
        val pinnedTime = Date(c.getLong(4))
        val hide = c.getInt(5) > 0
        val result = Site(name, baseUrl, logo, enabled, pinnedTime, hide)
        println(result)
        assertEquals(site, result)
    }

    companion object {
        private const val TEST_DB = "migration-test"
    }

}