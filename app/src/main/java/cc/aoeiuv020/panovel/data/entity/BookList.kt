package cc.aoeiuv020.panovel.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import cc.aoeiuv020.panovel.util.notNullOrReport
import java.util.*

/**
 * Created by AoEiuV020 on 2018.05.24-17:15:36.
 */

/**
 * 书单，
 */
@Entity(indices = [
    Index(
            value = ["uuid"],
            unique = true
    )
])
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
        val createTime: Date = Date(),
        /**
         * 唯一的uuid, 为了避免重复导入同一个书单造成重复，
         */
        val uuid: String = UUID.randomUUID().toString()
) {
    // id的非空版本，实在是要经常用id, 而且是不可能为空的id,
    val nId: Long get() = id.notNullOrReport()
}