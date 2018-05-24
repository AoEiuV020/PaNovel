package cc.aoeiuv020.panovel.data.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import cc.aoeiuv020.panovel.data.entity.Site

/**
 * Created by AoEiuV020 on 2018.05.24-14:20:59.
 */
@Dao
abstract class SiteDao {
    @Query("select * from Site")
    abstract fun list(): List<Site>

    /**
     * 插入前都有查询，所以不用在插入失败时尝试更新，
     */
    @Insert
    abstract fun insert(site: Site)

    @Query("update Site set enabled = :enabled where name = :name")
    abstract fun updateEnabled(name: String, enabled: Boolean)

    @Query("select * from Site where name = :name")
    abstract fun query(name: String): Site?
}