package cc.aoeiuv020.base.jar

import java.util.regex.Pattern

/**
 * Created by AoEiuV020 on 2018.04.18-10:24:40.
 */

// TODO: 改成支持kotlin的Regex,
fun String.pick(pattern: Pattern): List<String> {
    val matcher = pattern.matcher(this)
    return if (matcher.find()) {
        List(matcher.groupCount()) {
            matcher.group(it + 1)
        }
    } else {
        throw IllegalStateException("not match <$this, $pattern>")
    }
}

/**
 * 默认识别换行，整个字符串作为整体，
 */
fun String.pick(pattern: String) = pick(Pattern.compile(pattern, Pattern.DOTALL))
