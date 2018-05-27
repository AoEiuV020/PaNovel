package cc.aoeiuv020.irondb.impl

import cc.aoeiuv020.irondb.KeySerializer
import java.io.File

/**
 * 简单替换路径分隔符，因此若是名字仅这一处不同，将产生冲突，
 *
 * Created by AoEiuV020 on 2018.05.27-16:03:39.
 */
class ReplaceFileSeparator(
        private val replaceWith: Char = '|'
) : KeySerializer {
    override fun serialize(from: String): String {
        return from.replace(File.separatorChar, replaceWith)
    }
}