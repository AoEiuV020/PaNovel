package cc.aoeiuv020.panovel.presenter

import android.content.Context
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.*

/**
 *
 * Created by AoEiuV020 on 2017.10.02-21:42:59.
 */

fun <T : Any?> Observable<T>.async(): Observable<T> = this
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

private fun Context.ext() = File(getExternalFilesDir(null), "ext").apply { mkdirs() }
fun Context.save(name: String, obj: Serializable) {
    ObjectOutputStream(FileOutputStream(File(ext(), name))).run {
        writeObject(obj)
        close()
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> Context.load(name: String): T = ObjectInputStream(FileInputStream(File(ext(), name))).run {
    (readObject() as T).also { close() }
}
