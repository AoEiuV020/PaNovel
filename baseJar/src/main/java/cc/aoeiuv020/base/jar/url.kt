package cc.aoeiuv020.base.jar

import java.net.MalformedURLException
import java.net.URL

/**
 * Created by AoEiuV020 on 2018.09.09-02:39:00.
 */
/**
 * 地址仅路径，斜杆/开头，
 */
fun path(url: String): String = try {
    URL(url).path
} catch (e: MalformedURLException) {
    url
}
