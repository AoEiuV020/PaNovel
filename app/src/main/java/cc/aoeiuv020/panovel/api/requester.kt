@file:Suppress("unused")

package cc.aoeiuv020.panovel.api

import org.jsoup.Connection
import org.jsoup.Jsoup

/**
 * 请求类基类，
 * 以下几个类具体使用时可以再定义子类，
 * 自带extra一个字符串，
 * 一个open val url get() = extra，
 * 需要继承也不要拓展其他成员，
 * 本地保存只要类名和extra就可以，
 * 保证可序列化，
 * Serializable不咋地，SharedPreferences不支持，
 * 要保证可以直接调用构造方法传入且只传入extra字符串，
 * 这样可以只序列化保存类名和extra,
 * Created by AoEiuV020 on 2017.10.03-14:59:04.
 */
open class Requester(private var extra: String) {
    companion object {
        fun serialize(requester: Requester): String {
            return "${requester.javaClass.name}/${requester.extra}"
        }

        fun <T : Requester> deserialize(string: String): T {
            val index = string.indexOf('/')
            val name = string.substring(0, index)
            val extra = string.substring(index + 1)
            @Suppress("UNCHECKED_CAST")
            return Class.forName(name)
                    .getConstructor(String::class.java)
                    .newInstance(extra) as T
        }
    }

    val url get() = extra
    open fun request(): Connection.Response = Jsoup.connect(url).execute()
    override fun toString() = "${this.javaClass.simpleName}(url=$url)"
}

/**
 * 用来请求小说列表，
 */
open class ListRequester(url: String) : Requester(url)

/**
 * 用来请求小说详情页，
 */
open class DetailRequester(url: String) : Requester(url)

/**
 * 用来请求小说章节列表，
 */
open class ChaptersRequester(url: String) : Requester(url)

/**
 * 用来请求小说文本，
 */
open class TextRequester(url: String) : Requester(url)
