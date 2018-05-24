package cc.aoeiuv020.panovel.data.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.util.*

/**
 * Created by AoEiuV020 on 2018.05.24-17:15:36.
 */

/**
 * 书单，
 */
@Entity
data class BookList(
        @PrimaryKey(autoGenerate = true)
        val id: Long,
        val name: String,
        /**
         * 创建书单的时间，用于展示时排序，
         */
        val createTime: Date = Date()
)