package cc.aoeiuv020.irondb.impl

import cc.aoeiuv020.irondb.KeySerializer
import java.io.File

/**
 * 只用于判断元素是否存在集合中，
 * 不可用于读出元素，
 *
 * Created by AoEiuV020 on 2018.05.27-15:14:05.
 */
class KeysContainer(
        base: File,
        private val keySerializer: KeySerializer
) : Collection<String> {
    // 实际文件名列表，
    private val nameSet = base.list { _, name ->
        // 不包括包含的目录，
        !name.endsWith(File.separatorChar)
    }.toSet()

    override val size: Int = nameSet.size

    override fun contains(element: String): Boolean =
            nameSet.contains(keySerializer.serialize(element))

    override fun containsAll(elements: Collection<String>): Boolean {
        for (element in elements) {
            if (!contains(element)) {
                return false
            }
        }
        return true
    }

    override fun isEmpty(): Boolean = nameSet.isEmpty()

    // 不打算支持，
    override fun iterator(): Iterator<String> = throw UnsupportedOperationException()
}
