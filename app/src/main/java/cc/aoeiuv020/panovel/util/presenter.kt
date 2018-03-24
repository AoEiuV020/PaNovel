package cc.aoeiuv020.panovel.util

import cc.aoeiuv020.panovel.local.Settings
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.atomic.AtomicInteger

/**
 *
 * Created by AoEiuV020 on 2017.10.02-21:42:59.
 */

/**
 * 自定义Executor主要是为了避免线程复用时interrupt中断旧线程导致数据异常，
 */
private val asyncExecutor = object : Executor {
    private val threadNumber = AtomicInteger()
    val tasks = LinkedBlockingDeque<Runnable>()
    val max: Int get() = Settings.asyncThreadCount
    val threads = LinkedList<Thread>()
    val threadsIdle = mutableSetOf<Int>()
    val runnable: Runnable = Runnable {
        val id = threadNumber.getAndIncrement()
        while (true) {
            Thread.currentThread().name = "async-$id"
            threadsIdle.add(id)
            val r = tasks.take()
            threadsIdle.remove(id)
            r.run()
        }
    }

    fun newThread() = Thread(runnable)

    @Synchronized
    override fun execute(command: Runnable) {
        if (threadsIdle.isEmpty() && threads.size < max) {
            threads.push(newThread().apply { start() })
        }
        tasks.push(command)
    }
}

fun <T : Any?> Observable<T>.async(): Observable<T> = this
        .subscribeOn(Schedulers.from(asyncExecutor))
        .observeOn(AndroidSchedulers.mainThread())

fun <T : Any?> Single<T>.async(): Single<T> = this
        .subscribeOn(Schedulers.from(asyncExecutor))
        .observeOn(AndroidSchedulers.mainThread())

fun <T> ignoreException(block: () -> T?): Boolean = try {
    block()
    true
} catch (_: Exception) {
    false
}
