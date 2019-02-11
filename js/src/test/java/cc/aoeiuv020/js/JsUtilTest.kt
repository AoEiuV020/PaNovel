package cc.aoeiuv020.js

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by AoEiuV020 on 2019.02.11-16:55:05.
 */
class JsUtilTest {

    @Test
    fun run() {
        val start = System.currentTimeMillis()
        assertEquals("hello", JsUtil.run("(function(){return 'hello';}())"))
        // 180, 第一次有初始化环境，
        println(System.currentTimeMillis() - start)
        assertEquals("hello", JsUtil.run("(function(){return 'hello';}())"))
        // 184, 后面没有浪费太多时间，
        println(System.currentTimeMillis() - start)
        assertEquals("hello", JsUtil.run("(function(){return 'hello';}())"))
        println(System.currentTimeMillis() - start)
        assertEquals("hello", JsUtil.run("(function(){return 'hello';}())"))
        println(System.currentTimeMillis() - start)
        assertEquals("hello", JsUtil.run("(function(){return 'hello';}())"))
        println(System.currentTimeMillis() - start)
        assertEquals("hello", JsUtil.run("(function(){return 'hello';}())"))
        println(System.currentTimeMillis() - start)
    }
}