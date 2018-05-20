package cc.aoeiuv020.panovel.api.base

import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.api.NovelSite
import cc.aoeiuv020.panovel.api.firstIntPattern
import cc.aoeiuv020.panovel.api.firstTwoIntPattern
import org.jsoup.Connection
import org.jsoup.nodes.Element
import java.util.regex.Pattern

/**
 * Created by AoEiuV020 on 2018.05.20-16:26:44.
 */
@Suppress("ClassName", "LocalVariableName")
abstract class DslJsoupNovelContext : JsoupNovelContext() {
    /*
    *************** member ***************
     */
    override var detailTemplate: String? = null
        /**
         * 详情页地址模板默认同时赋值给目录页，
         */
        set(value) {
            if (chapterTemplate == null) {
                chapterTemplate = value
            }
            field = value
        }
    override var chapterTemplate: String? = null
    override var contentTemplate: String? = null
    override var bookIdRegex: Pattern = firstIntPattern
    override var chapterIdRegex: Pattern = firstTwoIntPattern

    override var charset: String = "UTF-8"

    /*
    *************** site ***************
     */
    override lateinit var site: NovelSite

    protected fun site(init: _NovelSite.() -> Unit) {
        _NovelSite().also { _site ->
            _site.init()
            site = _site.createNovelSite()
        }
    }

    protected class _NovelSite {
        lateinit var name: String
        lateinit var baseUrl: String
        lateinit var logo: String
        var enabled: Boolean = true
        fun createNovelSite(): NovelSite = NovelSite(
                name = name,
                baseUrl = baseUrl,
                logo = logo,
                enabled = enabled
        )
    }

    /*
    *************** search ***************
     */
    override fun searchNovelName(name: String): List<NovelItem> = _Search().run {
        initSearch(name)
        return requireNotNull(result)
    }

    private lateinit var initSearch: _Search.(String) -> Unit
    protected fun search(init: _Search.(String) -> Unit) {
        initSearch = init
    }

    protected inner class _Search
        : _Requester<List<NovelItem>, _NovelItemListParser>(_NovelItemListParser())


    /*
    *************** parser ***************
     */
    protected inner class _NovelItemListParser : _Parser<List<NovelItem>>() {
        private lateinit var _items: List<NovelItem>
        fun items(query: String, init: _NovelItemParser.() -> Unit): List<NovelItem> {
            return root.requireElements(query).map {
                val itemParser = _NovelItemParser()
                itemParser.root = it
                itemParser.init()
                itemParser.parse()
            }.also { _items = it }
        }

        override fun parse(): List<NovelItem> = _items
    }

    protected inner class _NovelItemParser : _Parser<NovelItem>() {
        private val _novelItem = _NovelItem()
        fun name(query: String, block: (Element) -> String = { it.text() }) {
            _novelItem.name = root.requireElement(query = query, name = TAG_NOVEL_NAME) {
                // 尝试从该元素中提取bookId，如果能成功，就不需要调用extra块，
                if (_novelItem.extra == null) {
                    try {
                        _novelItem.extra = findBookId(it.absHref())
                    } catch (e: Exception) {
                    }
                }
                block(it)
            }
        }

        fun author(query: String, block: (Element) -> String = { it.text() }) {
            _novelItem.author = root.requireElement(query = query, name = TAG_AUTHOR_NAME, block = block)
        }

        fun extra(query: String, block: (Element) -> String = { it.absHref() }) {
            _novelItem.extra = root.requireElement(query = query, name = TAG_NOVEL_LINK, block = block)
        }

        override fun parse(): NovelItem = _novelItem.createNovelItem()

        private inner class _NovelItem {
            var site: String = this@DslJsoupNovelContext.site.name
            var name: String? = null
            var author: String? = null
            var extra: String? = null
            fun createNovelItem() = NovelItem(
                    site,
                    requireNotNull(name),
                    requireNotNull(author),
                    requireNotNull(extra))
        }
    }

    protected abstract inner class _Parser<out T> {
        lateinit var root: Element
        abstract fun parse(): T?
    }

    /*
    *************** requester ***************
     */
    protected abstract inner class _Requester<T, out R : _Parser<T>>(
            private val parser: R
    ) {
        lateinit var connection: Connection
        fun get(init: _Request.() -> Unit): Connection = _Request().run {
            method = Connection.Method.GET
            init()
            createConnection().also {
                connection = it
            }
        }

        fun post(init: _Request.() -> Unit): Connection = _Request().run {
            method = Connection.Method.POST
            init()
            createConnection().also {
                connection = it
            }
        }

        var result: T? = null
        fun document(init: R.() -> Unit): T? {
            parser.root = parse(connection)
            parser.init()
            return parser.parse().also { result = it }
        }
    }

    protected inner class _Request {
        var url: String? = null
        var method: Connection.Method? = null
        var dataMap: Map<String, String>? = null
        fun createConnection(): Connection = connect(absUrl(requireNotNull(url)))
                .method(requireNotNull(method))
                .apply { if (dataMap != null) data(dataMap) }

        /**
         * TODO: 这种方法装载参数的话，如果是get, jsoup写死URLEncode编码utf-8, 整个改okhttp吧，
         */
        @Deprecated("如果是get, jsoup写死URLEncode编码utf-8, 先别用，")
        fun data(init: _Data.() -> Unit) {
            _Data().also { _data ->
                _data.init()
                dataMap = _data.createDataMap()
            }
        }

        inner class _Data {
            val map: MutableMap<String, String> = mutableMapOf()
            fun createDataMap(): Map<String, String> = map
            operator fun Pair<String, String>.unaryPlus() {
                map[first] = second
            }
        }
    }
}
