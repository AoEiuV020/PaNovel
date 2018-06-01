package cc.aoeiuv020.base.jar

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Created by AoEiuV020 on 2018.06.01-15:32:50.
 */
/**
 * 供大量io异步操作使用，
 * 繁重的异步任务不能和其他简单异步混用线程池，可能导致阻塞，
 * 比如anko-common的doAsync默认使用anko自带的BackgroundExecutor，
 * 如果下载线程过多，任务过重，会导致anko的BackgroundExecutor阻塞，影响其他异步操作，
 */
val ioExecutorService: ExecutorService by lazy {
    Executors.newCachedThreadPool()
}
