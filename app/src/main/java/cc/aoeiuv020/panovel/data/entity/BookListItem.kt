package cc.aoeiuv020.panovel.data.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index

/**
 * Created by AoEiuV020 on 2018.05.24-16:18:20.
 */
@Entity(
        // 主键可以当成bookListId的索引，
        primaryKeys = ["bookListId", "novelId"],
        // 警告说两个外键都要索引，这里补上novelId的索引，
        // 大概是操作父字段时要查询字字段，所以建议加上索引，
        // 没什么必要的感觉，并不打算删改Novel表中的id,
        indices = [
            (Index(
                    value = ["novelId"],
                    unique = false
            ))
        ],
        foreignKeys = [
            (ForeignKey(
                    entity = BookList::class,
                    parentColumns = ["id"],
                    childColumns = ["bookListId"],
                    onDelete = ForeignKey.CASCADE
            )),
            (ForeignKey(
                    entity = Novel::class,
                    parentColumns = ["id"],
                    childColumns = ["novelId"],
                    onDelete = ForeignKey.CASCADE
            ))
        ]
)
data class BookListItem(
        /**
         * 书单的名字，建个索引，同名的认为是同一个书单，
         */
        val bookListId: Long,
        /**
         * 外键id, 对应小说表中的id,
         */
        val novelId: Long
)