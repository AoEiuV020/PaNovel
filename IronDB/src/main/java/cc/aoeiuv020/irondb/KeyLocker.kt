package cc.aoeiuv020.irondb

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Semaphore

/**
 * Created by AoEiuV020 on 2018.05.27-15:18:53.
 */
@Suppress("MemberVisibilityCanBePrivate")
class KeyLocker {
    // 没有删除不再被使用的entry, 可能浪费内存，
    private val semaphoreMap = ConcurrentHashMap<String, Semaphore>()

    fun acquire(key: String) {
        val semaphore = semaphoreMap.getOrPut(key) { Semaphore(1, true) }
        semaphore.acquireUninterruptibly()
    }

    fun release(key: String) {
        semaphoreMap[key]?.release()
    }

    fun <T> runInAcquire(key: String, block: () -> T?): T? {
        acquire(key)
        val ret = block()
        release(key)
        return ret
    }
}