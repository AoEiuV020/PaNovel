package cc.aoeiuv020.panovel.presenter

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 *
 * Created by AoEiuV020 on 2017.10.02-21:42:59.
 */

fun <T : Any?> Observable<T>.async(): Observable<T> = this
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
