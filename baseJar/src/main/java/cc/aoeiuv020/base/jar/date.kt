package cc.aoeiuv020.base.jar

import java.util.*
import java.util.concurrent.TimeUnit.DAYS

/**
 * Created by AoEiuV020 on 2018.05.31-22:35:08.
 */

/**
 * 判断时间是否有效，
 * 考虑到时区问题，不能直接判断是否等于0,
 * 改为判断是否小于一天，
 * 大于一天的有效，
 */
fun Date.notZero(): Date? = this.takeIf { time > DAYS.toMillis(1) }