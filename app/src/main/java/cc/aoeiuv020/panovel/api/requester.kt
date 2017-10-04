@file:Suppress("unused")

package cc.aoeiuv020.panovel.api

import org.jsoup.Connection
import org.jsoup.Jsoup
import java.io.Serializable

/**
 * 请求类基类，
 * 以下几个类具体使用时可以再定义子类，
 * 尽量保证可序列化，
 * Serializable不咋地，SharedPreferences不支持，
 * Created by AoEiuV020 on 2017.10.03-14:59:04.
 */
open class Requester(val url: String) : Serializable {
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
