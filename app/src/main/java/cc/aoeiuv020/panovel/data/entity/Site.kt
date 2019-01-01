package cc.aoeiuv020.panovel.data.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.util.*

/**
 * Created by AoEiuV020 on 2018.05.24-14:16:53.
 */
/**
 * 小说网站信息，
 */
@Entity
data class Site(
        @PrimaryKey
        val name: String,
        var baseUrl: String,
        var logo: String,
        // 启用，true表示全站搜索时包括这个站，
        var enabled: Boolean = true,
        /**
         * 置顶时间，网站列表按这个排序，
         * 不置顶的给个最小时间，
         */
        var pinnedTime: Date = Date(0),
        // 隐藏，true列表在网站列表里看不到，
        var hide: Boolean = false
)
