package cc.aoeiuv020.js

import org.mozilla.javascript.Context

/**
 * Created by AoEiuV020 on 2019.02.11-16:48:42.
 */
object JsUtil {
    private fun getContext() = Context.enter().apply {
        // 禁用这个优化才能在安卓使用，
        optimizationLevel = -1
    }
    fun run(js: String): String {
        val ctx = getContext()
        val scope = ctx.initStandardObjects()
        val result = ctx.evaluateString(scope, js, "<string>", 1, null)
        return Context.toString(result)
    }

    fun create(): JsContext = JsContext.create(getContext())

    fun release() = Context.exit()
}