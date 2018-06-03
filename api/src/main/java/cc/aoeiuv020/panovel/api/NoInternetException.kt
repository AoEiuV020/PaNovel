package cc.aoeiuv020.panovel.api

/**
 * 归到断网的异常，不上报，
 *
 * Created by AoEiuV020 on 2018.06.03-08:38:46.
 */
class NoInternetException(cause: Throwable)
    : RuntimeException("没有连接网络，", cause)