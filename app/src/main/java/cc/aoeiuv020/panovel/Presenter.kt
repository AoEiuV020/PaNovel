package cc.aoeiuv020.panovel

/**
 * mvp的presenter,
 * Created by AoEiuV020 on 2017.10.11-15:32:17.
 */
abstract class Presenter<T : IView> {
    protected var view: T? = null

    fun attach(view: T) {
        this.view = view
    }

    fun detach() {
        view = null
    }
}