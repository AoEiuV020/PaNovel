package cc.aoeiuv020.js

import org.mozilla.javascript.Context
import org.mozilla.javascript.ScriptableObject

/**
 * Created by AoEiuV020 on 2019.02.11-18:08:04.
 */
class JsContext private constructor(
        private val ctx: Context
) {
    companion object {
        internal fun create(ctx: Context): JsContext = JsContext(ctx)
    }

    private val scope: ScriptableObject = ctx.initStandardObjects()

    fun run(js: String): String {
        val result = ctx.evaluateString(scope, js, "<string>", 1, null)
        return Context.toString(result)
    }

}