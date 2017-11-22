package cc.aoeiuv020.panovel

import io.reactivex.disposables.Disposable
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug

/**
 * mvp的presenter,
 * Created by AoEiuV020 on 2017.10.11-15:32:17.
 */
abstract class Presenter<T : IView> : AnkoLogger {
    /**
     * 每个presenter维护一组observable，每个位置只能有一个，
     * 顶掉上一个比避免过时的数据展示出来，
     * detach时全部取消，
     */
    private val disposableList = ArrayList<Disposable?>()
    var view: T? = null
        private set

    fun attach(view: T) {
        debug { "$this attach $view" }
        this.view = view
    }

    fun detach() {
        debug { "$this detach $view" }
        disposableList.forEach { it?.dispose() }
        view = null
    }

    protected fun addDisposable(disposable: Disposable, index: Int = 0) {
        while (index >= disposableList.size) {
            disposableList.add(null)
        }
        val old = disposableList[index]
        disposableList[index] = disposable
        old?.dispose()
    }

    override fun toString(): String = "${javaClass.simpleName}@${hashCode()}"
}