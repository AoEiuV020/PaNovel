package cc.aoeiuv020.panovel

import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug

/**
 * mvp的presenter,
 * Created by AoEiuV020 on 2017.10.11-15:32:17.
 */
abstract class Presenter<T : IView> : AnkoLogger {
    protected var view: T? = null

    fun attach(view: T) {
        debug { "$this attach $view" }
        this.view = view
    }

    fun detach() {
        debug { "$this detach $view" }
        view = null
    }

    override fun toString(): String = "${javaClass.simpleName}@${hashCode()}"
}