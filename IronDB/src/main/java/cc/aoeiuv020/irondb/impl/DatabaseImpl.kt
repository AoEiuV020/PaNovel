package cc.aoeiuv020.irondb.impl

import cc.aoeiuv020.irondb.DataSerializer
import cc.aoeiuv020.irondb.Database
import cc.aoeiuv020.irondb.KeyLocker
import cc.aoeiuv020.irondb.KeySerializer
import java.io.File
import java.io.IOException
import java.lang.reflect.Type

/**
 * Created by AoEiuV020 on 2018.05.27-16:11:58.
 */
internal class DatabaseImpl(
        private val base: File,
        private val subSerializer: KeySerializer,
        private val keySerializer: KeySerializer,
        private val dataSerializer: DataSerializer
) : Database {
    private val keyLocker = KeyLocker()

    init {
        base.exists() || base.mkdirs() || throw IOException("failed mkdirs ${base.path}")
    }

    override fun sub(table: String) = DatabaseImpl(
            base = base.resolve(table).canonicalFile,
            subSerializer = subSerializer,
            keySerializer = keySerializer,
            dataSerializer = dataSerializer
    )


    override fun <T> write(key: String, value: T?, type: Type) {
        val serializedKey = keySerializer.serialize(key)
        val file = base.resolve(serializedKey)
        // 锁住key,
        keyLocker.runInAcquire(serializedKey) {
            if (value == null) {
                // 值空则删除对应文件，
                file.delete()
            } else {
                val data = dataSerializer.serialize(value, type)
                file.writeText(data)
            }
        }
    }

    /**
     * @return key不存在则返回null,
     */
    override fun <T> read(key: String, type: Type): T? {
        val serializedKey = keySerializer.serialize(key)
        val file = base.resolve(serializedKey)
        // 锁住key,
        return keyLocker.runInAcquire(serializedKey) {
            if (!file.exists()) {
                // 文件不存在直接返回null,
                null
            } else {
                val string = file.readText()
                dataSerializer.deserialize(string, type)
            }
        }
    }

    /**
     * @return 返回用于判断指定key是否存在的集合，不可用于读出key,
     */
    override fun keysContainer(): Collection<String> = KeysContainer(base, keySerializer)
}
