package cc.aoeiuv020.panovel.data.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import cc.aoeiuv020.panovel.data.entity.SiteEnabled

/**
 * Created by AoEiuV020 on 2018.05.13-17:58:21.
 */
@Dao
abstract class SiteEnabledDao {
    @Query("select * from SiteEnabled")
    abstract fun list(): List<SiteEnabled>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(site: SiteEnabled)
}