package cc.aoeiuv020.panovel.data.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import cc.aoeiuv020.panovel.util.notNull
import java.util.*

/**
 * Created by AoEiuV020 on 2018.05.24-17:15:36.
 */

/**
 * 书单，
 */
@Entity
data class BookList(
        /**
         * 普通的id,
         * 要给个null才能autoGenerate，
         * 插入时拿到id再赋值回来，所以要可变var，
         */
        @PrimaryKey(autoGenerate = true)
        val id: Long? = null,
        val name: String,
        /**
         * 创建书单的时间，用于展示时排序，
         */
        val createTime: Date = Date()
) {
    // id的非空版本，实在是要经常用id, 而且是不可能为空的id,
    val nId: Long get() = id.notNull()
}