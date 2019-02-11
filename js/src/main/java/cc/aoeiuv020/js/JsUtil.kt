package cc.aoeiuv020.js

import org.mozilla.javascript.Context

/**
 * Created by AoEiuV020 on 2019.02.11-16:48:42.
 */
object JsUtil {
    fun run(js: String): String {
        val ctx = Context.enter()
        val scope = ctx.initStandardObjects()
        val result = ctx.evaluateString(scope, js, "<cmd>", 1, null)
        return Context.toString(result)
    }
}