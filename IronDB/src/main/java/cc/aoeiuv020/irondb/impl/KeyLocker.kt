package cc.aoeiuv020.irondb.impl

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
        // key不存在就什么都不做，
        semaphoreMap[key]?.release()
    }

    fun <T> runInAcquire(key: String, block: () -> T): T = try {
        acquire(key)
        block()
    } finally {
        // 放在finally以防万一io异常时也要释放锁，
        release(key)
    }
}