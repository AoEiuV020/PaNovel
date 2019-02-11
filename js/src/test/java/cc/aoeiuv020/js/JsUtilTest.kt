package cc.aoeiuv020.js

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by AoEiuV020 on 2019.02.11-16:55:05.
 */
class JsUtilTest {

    @Test
    fun run() {
        assertEquals("hello", JsUtil.run("(function(){return 'hello';}())"))
    }
}