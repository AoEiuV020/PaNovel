package cc.aoeiuv020.panovel.refresher

import java.util.concurrent.TimeUnit

data class Config(
        /**
         * 每轮的最小时间，太快了就休息到够，
         */
        val minTime: Long = TimeUnit.MINUTES.toMillis(10),
        /**
         * 目标时间，希望每轮执行时间，
         */
        val targetTime: Long = TimeUnit.MINUTES.toMillis(30),
        /**
         * 一轮最多拿这么多个，
         */
        val maxSize: Int = 100,
        /**
         * 是否必须成功获取书架，
         * 否则直接让程序崩溃，
         */
        val requireBookshelf: Boolean = true,
        /**
         * 调试模式，
         */
        val debug: Boolean = false,
        /**
         * 并发请求线程数，
         */
        val threads: Int = 10,
        /**
         * 禁用的网站名，
         */
        val disableSites: Set<String> = setOf()
)