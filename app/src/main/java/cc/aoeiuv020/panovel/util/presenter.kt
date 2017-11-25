package cc.aoeiuv020.panovel.util

import cc.aoeiuv020.panovel.local.Settings
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
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
    val threads = List(Settings.asyncThreadCount) {
        newThread()
    }

    init {
        threads.forEach {
            it.start()
        }
    }

    fun newThread() = Thread({
        while (true) {
            tasks.take().run()
        }
    }, "async-${threadNumber.getAndIncrement()}")

    override fun execute(command: Runnable) {
        tasks.push(command)
    }
}

fun <T : Any?> Observable<T>.async(): Observable<T> = this
        .subscribeOn(Schedulers.from(asyncExecutor))
        .observeOn(AndroidSchedulers.mainThread())

fun <T> ignoreException(block: () -> T?) {
    try {
        block()
    } catch (_: Exception) {
    }
}