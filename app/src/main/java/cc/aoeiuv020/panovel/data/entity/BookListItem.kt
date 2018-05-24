package cc.aoeiuv020.panovel.data.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey

/**
 * Created by AoEiuV020 on 2018.05.24-16:18:20.
 */
@Entity(
        primaryKeys = ["bookListId", "novelId"],
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