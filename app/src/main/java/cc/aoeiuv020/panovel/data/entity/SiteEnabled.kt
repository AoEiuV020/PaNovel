package cc.aoeiuv020.panovel.data.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * 保存网站是否启用的列表，
 *
 * Created by AoEiuV020 on 2018.05.13-17:56:12.
 */
@Entity
@Suppress("MemberVisibilityCanBePrivate", "unused")
data class SiteEnabled(
        @PrimaryKey
        val name: String,
        val enabled: Boolean = true
)