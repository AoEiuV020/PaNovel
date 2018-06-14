package cc.aoeiuv020.irondb

import cc.aoeiuv020.base.jar.type
import java.lang.reflect.Type

/**
 * Created by AoEiuV020 on 2018.05.27-14:43:23.
 */
interface Database {
    fun sub(table: String): Database
    /**
     * @param value 为空则删除对应文件，
     */
    fun <T> write(key: String, value: T?, type: Type)

    /**
     * @return key不存在则返回null,
     */
    fun <T> read(key: String, type: Type): T?

    /**
     * 封装文件的使用，确保线程安全，
     */
    fun file(key: String): FileWrapper

    fun drop()

    /**
     * @return 返回用于判断指定key是否存在的集合，不可用于读出key,
     */
    fun keysContainer(): Collection<String>

}

/**
 * 通过gson的TypeToken得到T的真实类型，
 * 这里不是必须的，
 */
@Suppress("unused")
inline fun <reified T> Database.write(key: String, value: T) = write(key, value, type<T>())

/**
 * 通过gson的TypeToken得到T的真实类型，
 */
@Suppress("unused")
inline fun <reified T> Database.read(key: String): T? = read(key, type<T>())
