package cc.aoeiuv020.irondb

import cc.aoeiuv020.irondb.impl.DatabaseImpl
import cc.aoeiuv020.irondb.impl.GsonSerializer
import cc.aoeiuv020.irondb.impl.ReplaceFileSeparator
import java.io.File

/**
 * Created by AoEiuV020 on 2018.05.27-14:42:39.
 */
object Iron {
    fun db(
            base: File,
            // 默认使用gson进行序列化，
            dataSerializer: DataSerializer = GsonSerializer(),
            // 默认简单替换文件分隔符，
            keySerializer: KeySerializer = ReplaceFileSeparator(),
            subSerializer: KeySerializer = keySerializer
    ): Database = DatabaseImpl(
            base = base,
            subSerializer = subSerializer,
            keySerializer = keySerializer,
            dataSerializer = dataSerializer
    )
}