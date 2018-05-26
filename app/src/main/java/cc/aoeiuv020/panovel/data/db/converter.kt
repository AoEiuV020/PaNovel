package cc.aoeiuv020.panovel.data.db

import android.arch.persistence.room.TypeConverter
import java.util.*


/**
 * Created by AoEiuV020 on 2018.05.26-22:43:21.
 */
class DateTypeConverter {

    @TypeConverter
    fun toDate(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun toLong(value: Date?): Long? {
        return value?.time
    }
}