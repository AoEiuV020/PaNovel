package cc.aoeiuv020.irondb

import java.io.File
import java.lang.reflect.Type

/**
 * 用于将key序列化成合法的文件路径，
 * 重点是不能有路径分隔符[File.separatorChar],
 * 只需要序列化，不需要反序列化，因此可以使用md5之类的，
 *
 * Created by AoEiuV020 on 2018.05.27-14:55:59.
 */
interface KeySerializer {
    fun serialize(from: String): String
}

/**
 * 用于对象序列化，
 */
interface DataSerializer {
    fun <T> serialize(value: T, type: Type): String
    fun <T> deserialize(string: String, type: Type): T
}