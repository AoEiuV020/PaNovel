@file:Suppress("unused")

package cc.aoeiuv020.panovel.api

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonObject
import com.google.gson.JsonSerializer
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
open class Requester(val extra: String) {
    companion object {
        fun attach(builder: GsonBuilder): GsonBuilder = builder.apply {
            registerTypeHierarchyAdapter(Requester::class.java, JsonSerializer { src: Requester, _, _ ->
                JsonObject().apply {
                    addProperty("type", src.javaClass.name)
                    addProperty("extra", src.extra)
                }
            })
            registerTypeHierarchyAdapter(Requester::class.java, JsonDeserializer { json, _, _ ->
                json.asJsonObject.let {
                    val type = it.getAsJsonPrimitive("type").asString
                    val extra = it.getAsJsonPrimitive("extra").asString
                    val clazz = Class.forName(type)
                    try {
                        val mNew = clazz.getMethod("new", String::class.java)
                        mNew.invoke(null, extra) as Requester
                    } catch (_: Exception) {
                        // 可能没有new方法，也可能不是静态方法，
                        // 静态new方法和构造方法总要有一个，
                        clazz.getConstructor(String::class.java)
                                .newInstance(extra) as Requester
                    }
                }
            })
        }
    }

    open val url get() = extra
    open fun connect(): Connection = Jsoup.connect(url)
    override fun toString() = "${this.javaClass.simpleName}(url=$url)"
    override fun equals(other: kotlin.Any?): Boolean {
        return if (other == null || other !is Requester) false
        else other.javaClass == javaClass && other.extra == extra
    }

    override fun hashCode(): Int {
        return extra.hashCode()
    }
}

/**
 * 用来请求小说列表，
 */
abstract class ListRequester(url: String) : Requester(url)

/**
 * 用来请求分类页面的小说列表，
 */
open class GenreListRequester(url: String) : ListRequester(url)

/**
 * 用来请求搜索结果页面的小说列表，
 */
open class SearchListRequester(url: String) : ListRequester(url)

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
