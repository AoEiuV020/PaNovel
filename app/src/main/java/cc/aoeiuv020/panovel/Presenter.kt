package cc.aoeiuv020.panovel

import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.verbose

/**
 * mvp的presenter,
 * Created by AoEiuV020 on 2017.10.11-15:32:17.
 */
abstract class Presenter<T : IView> : AnkoLogger {
    var view: T? = null
        private set

    fun attach(view: IView) {
        verbose { "$this attach $view" }
        @Suppress("UNCHECKED_CAST")
        this.view = view as? T
    }

    fun detach() {
        verbose { "$this detach $view" }
        view = null
    }

    override fun toString(): String = "${javaClass.simpleName}@${hashCode()}"
}