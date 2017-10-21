package cc.aoeiuv020.panovel.util

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executor
import java.util.concurrent.LinkedBlockingDeque

/**
 *
 * Created by AoEiuV020 on 2017.10.02-21:42:59.
 */

private val asyncExecutor = object : Executor {
    val tasks = LinkedBlockingDeque<Runnable>()
    val threads = List(4) {
        newThread(it)
    }

    init {
        threads.forEach {
            it.start()
        }
    }

    fun newThread(index: Int) = Thread({
        while (true) {
            tasks.take().run()
        }
    }, "async-$index")

    override fun execute(command: Runnable) {
        tasks.push(command)
    }
}

fun <T : Any?> Observable<T>.async(): Observable<T> = this
        .subscribeOn(Schedulers.from(asyncExecutor))
        .observeOn(AndroidSchedulers.mainThread())
