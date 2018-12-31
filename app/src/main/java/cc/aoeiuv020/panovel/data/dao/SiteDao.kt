package cc.aoeiuv020.panovel.data.dao

import android.arch.persistence.room.*
import cc.aoeiuv020.panovel.data.entity.Site
import java.util.*

/**
 * Created by AoEiuV020 on 2018.05.24-14:20:59.
 */
@Dao
abstract class SiteDao {
    @Query("select * from Site where hide = 0 order by pinnedTime desc, name asc")
    abstract fun list(): List<Site>

    /**
     * 同步网站列表时删除已经不再支持的网站，
     */
    @Delete
    abstract fun removeSite(site: Site)

    /**
     * 插入前都有查询，所以不用在插入失败时尝试更新，
     */
    @Insert
    abstract fun insert(site: Site)

    @Query("update Site set enabled = :enabled where name = :name")
    abstract fun updateEnabled(name: String, enabled: Boolean)

    @Query("select * from Site where name = :name")
    abstract fun query(name: String): Site?

    @Query("select count(*) from Site where name = :name")
    abstract fun checkSiteSupport(name: String): Boolean

    @Query("update Site set baseUrl = :baseUrl, logo = :logo where name = :name")
    abstract fun updateSiteInfo(name: String, baseUrl: String, logo: String)

    @Query("update Site set pinnedTime = :pinnedTime where name = :name")
    abstract fun updatePinnedTime(name: String, pinnedTime: Date)

    @Query("update Site set hide = :hide where name = :name")
    abstract fun updateHide(name: String, hide: Boolean)

    @Update
    abstract fun updateSite(site: Site)
}