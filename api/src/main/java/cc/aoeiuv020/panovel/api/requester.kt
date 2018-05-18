@file:Suppress("unused")

package cc.aoeiuv020.panovel.api

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.net.MalformedURLException
import java.net.URL

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
 *
 * Created by AoEiuV020 on 2017.10.03-14:59:04.
 */
open class Requester(val extra: String) {
    companion object {
        /**
         * @param type 完整类名，
         */
        fun deserialization(type: String, extra: String): Requester {
            val clazz = try {
                Class.forName(type)
            } catch (e: ClassNotFoundException) {
                // 类型不存在可能是已经废弃了的那一系列，对应的extra都是正常url,
                val url = try {
                    URL(extra)
                } catch (e: MalformedURLException) {
                    throw IllegalStateException("Requester类型不存在<$type, $extra>")
                }
                return Requester(url.toString())
            }
            return try {
                val mNew = clazz.getMethod("new", String::class.java)
                mNew.invoke(null, extra) as Requester
            } catch (_: Exception) {
                // 可能没有new方法，也可能不是静态方法，
                // 静态new方法和构造方法总要有一个，
                clazz.getConstructor(String::class.java)
                        .newInstance(extra) as Requester
            }
        }

        /**
         * 用来分开Requester的类名和参数，
         * 所以不能是可能存在类名里的字符，
         */
        private const val dividerCharacter = '|'

        fun attach(builder: GsonBuilder): GsonBuilder = builder.apply {
            registerTypeHierarchyAdapter(Requester::class.java, JsonSerializer { src: Requester, _, _ ->
                // typeOfT就是src和类型，就算src在包装类里也一样，也就是没用，
                val packageName = Requester::class.java.`package`.name
                val className = src.javaClass.name
                // 设置默认包名，小数点.开头表示用默认包名，省空间，
                val type = if (className.startsWith(packageName)) {
                    className.removePrefix(packageName)
                } else {
                    className
                }
                JsonPrimitive("$type$dividerCharacter${src.extra}")
            })
            registerTypeHierarchyAdapter(Requester::class.java, JsonDeserializer { json, _, _ ->
                // typeOfT是目标对象的类型，
                when {
                    json.isJsonObject -> // 兼容旧版，
                        json.asJsonObject.let {
                            val type = it.getAsJsonPrimitive("type").asString
                            val extra = it.getAsJsonPrimitive("extra").asString
                            deserialization(type, extra)
                        }
                    json.isJsonPrimitive -> {
                        val typeWithExtra = json.asString
                        val dividerIndex = typeWithExtra.indexOf(dividerCharacter).also {
                            if (it == -1) {
                                throw IllegalStateException("Requester不合法，没有分隔符'|'，")
                            }
                        }
                        val type = typeWithExtra.substring(0, dividerIndex)
                        // 如果extra为空，这里的substring可以正常返回空字符串，
                        val extra = typeWithExtra.substring(dividerIndex + 1)
                        // 恢复默认包名，小数点.开头表示用默认包名，
                        val className = if (type.startsWith('.')) {
                            val packageName = Requester::class.java.`package`.name
                            "$packageName$type"
                        } else {
                            type
                        }
                        deserialization(className, extra)
                    }
                    else -> throw IllegalStateException("Requester格式不正确，")
                }
            })
        }
    }

    val type: String get() = this.javaClass.name
    /**
     * 外部调用网站上下文的方法来得到requester的真实地址，
     * [NovelContext.getAbsoluteUrl]
     * TODO: 改成internal，
     */
    open val url get() = extra
    open fun connect(): Connection = Jsoup.connect(url).maxBodySize(0)
    open fun doBeforeExecute(conn: Connection): Connection = conn
    override fun toString() = "${this.javaClass.simpleName}(url=$url)"
    override fun equals(other: kotlin.Any?): Boolean {
        return if (other == null || other !is Requester) false
        else other.javaClass == javaClass && other.extra == extra
    }

    override fun hashCode(): Int {
        return extra.hashCode()
    }
}

class PathOnlyRequester(path: String) : Requester(path) {

}
