package cc.aoeiuv020.panovel.api.base

import cc.aoeiuv020.js.JsContext
import cc.aoeiuv020.js.JsUtil
import org.jsoup.nodes.Element
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * Created by AoEiuV020 on 2019.02.11-18:27:45.
 */
abstract class JsNovelContext : DslJsoupNovelContext() {
    // js线程，一个js上下文只能一个线程使用，其他线程需要时都等待这个线程执行，
    private val jsExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val jsContext: JsContext = get { JsUtil.create() }

    @Suppress("UNCHECKED_CAST")
    private fun <T> submit(block: () -> T): Future<T> {
        return jsExecutor.submit(block) as Future<T>
    }

    private fun <T> get(block: () -> T): T {
        return submit(block).get()
    }

    protected fun js(string: String): String = get {
        jsContext.run(string)
    }

    protected fun Element.script(query: String) = requireElement(query, TAG_SCRIPT).data()
}