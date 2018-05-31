package cc.aoeiuv020.panovel.api.base

import cc.aoeiuv020.panovel.api.*
import org.jsoup.Connection
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/**
 * Created by AoEiuV020 on 2018.05.20-16:26:44.
 */
// MemberVisibilityCanBePrivate, 有不少预先准备的成员，可能暂时没有被使用，但是不能private,
// ClassName, LocalVariableName, 内部使用的类和变量通通下划线_开头，可能不符合规范，
@Suppress("ClassName", "LocalVariableName", "MemberVisibilityCanBePrivate")
abstract class DslJsoupNovelContext : JsoupNovelContext() {
    /*
    *************** member ***************
     */
    override var detailPageTemplate: String? = null
        /**
         * 详情页地址模板默认同时赋值给目录页，
         */
        set(value) {
            if (chaptersPageTemplate == null) {
                chaptersPageTemplate = value
            }
            field = value
        }
    override var chaptersPageTemplate: String? = null
    override var contentPageTemplate: String? = null
    override var bookIdRegex: Pattern = firstIntPattern
    override var chapterIdRegex: Pattern = firstTwoIntPattern

    override var charset: String? = null

    /*
    *************** site ***************
     */
    override lateinit var site: NovelSite

    /**
     * site这块是直接跑一遍存起结果，
     * 其他块都是存起lambda用的时候才调用，
     */
    protected fun site(init: _NovelSite.() -> Unit) {
        site = _NovelSite().run {
            init()
            createNovelSite()
        }
    }

    protected class _NovelSite {
        lateinit var name: String
        lateinit var baseUrl: String
        lateinit var logo: String
        fun createNovelSite(): NovelSite = NovelSite(
                name = name,
                baseUrl = baseUrl,
                logo = logo
        )
    }

    /*
    *************** search ***************
     */
    override fun searchNovelName(name: String): List<NovelItem> =
            _Search(name).initSearch(name)

    private lateinit var initSearch: _Search.(String) -> List<NovelItem>
    protected fun search(init: _Search.(String) -> List<NovelItem>) {
        initSearch = init
    }

    protected inner class _Search(name: String)
        : _Requester(name) {

        fun document(init: _NovelItemListParser.() -> Unit): List<NovelItem> =
                _NovelItemListParser(
                        parse(requireNotNull(connection), charset)
                ).also(init).parse()
    }

    protected inner class _NovelItemListParser(root: Element)
        : _Parser<List<NovelItem>>(root) {
        lateinit var novelItemList: List<NovelItem>
        fun single(init: _NovelItemParser.() -> Unit) {
            novelItemList = listOf(
                    _NovelItemParser(root).run {
                        // 搜索结果只有一个，认为是跳转到详情页了，没法用items, 默认直接从地址找bookId,
                        extra = findBookId(root.ownerDocument().location())
                        init()
                        parse()
                    }
            )
        }

        fun items(query: String, parent: Element = root, init: _NovelItemParser.() -> Unit) {
            novelItemList = parent.requireElements(query).mapNotNull {
                try {
                    _NovelItemParser(it).run {
                        init()
                        parse()
                    }
                } catch (e: Exception) {
                    null
                }
            }
        }

        override fun parse(): List<NovelItem> = novelItemList
    }

    /*
    *************** detail ***************
     */
    override fun getNovelDetail(extra: String): NovelDetail =
            _Detail(extra).initDetail(extra)

    private lateinit var initDetail: _Detail.(String) -> NovelDetail
    protected fun detail(init: _Detail.(String) -> NovelDetail) {
        initDetail = init
    }

    protected inner class _Detail(extra: String) : _Requester(extra) {
        fun document(
                document: Document = parse(connection
                        ?: connect(getNovelDetailUrl(extra)), charset),
                init: _NovelDetailParser.() -> Unit
        ): NovelDetail =
                _NovelDetailParser(extra, document).also(init).parse()
    }

    protected inner class _NovelDetailParser(
            // 有需要的话这个可以改public,
            private val detailExtra: String,
            root: Element
    ) : _Parser<NovelDetail>(root) {
        private val _novelDetail = _NovelDetail()

        init {
            // 默认直接将详情页的extra传给目录页，
            // 通常双方都是只需要一个bookId,
            _novelDetail.extra = findBookId(detailExtra)
        }

        var novel: NovelItem?
            get() = _novelDetail.novel
            set(value) {
                _novelDetail.novel = value
            }

        fun novel(init: _NovelItemParser.() -> Unit) {
            novel = _NovelItemParser(root).run {
                // 默认流程，findBookId(detailExtra) -> _novelDetail.extra -> novel.extra,
                extra = this@_NovelDetailParser._novelDetail.extra
                init()
                parse()
            }
        }

        var image: String?
            get() = _novelDetail.image
            set(value) {
                _novelDetail.image = value
            }

        fun image(query: String, parent: Element = root, block: (Element) -> String = { it.absSrc() }) {
            image = parent.requireElement(query = query, name = TAG_IMAGE, block = block)
        }

        var update: Date?
            get() = _novelDetail.update
            set(value) {
                _novelDetail.update = value
            }

        fun update(query: String, parent: Element = root, format: String, block: (Element) -> String = { it.text() }) =
                update(query = query, parent = parent) {
                    val updateString = block(it)
                    val sdf = SimpleDateFormat(format, Locale.CHINA)
                    sdf.parse(updateString)
                }

        fun update(query: String, parent: Element = root, block: (Element) -> Date) {
            update = parent.getElement(query = query, block = block)
        }

        var introduction: String?
            get() = _novelDetail.introduction
            set(value) {
                _novelDetail.introduction = value
            }

        fun introduction(
                query: String,
                parent: Element = root,
                block: (Element) -> String = { it.textList().joinToString("\n") }
        ) {
            introduction = parent.getElement(query = query, block = block)
        }

        var extra: String?
            get() = _novelDetail.extra
            set(value) {
                _novelDetail.extra = value
            }

        // 目录页一般可以靠bookId拼接地址搞定，如果不行，要调用这个方法，就很可能需要完整地址，所以默认用absHref,
        fun extra(query: String, parent: Element = root, block: (Element) -> String = { it.absHref() }) {
            extra = parent.requireElement(query = query, name = TAG_CHAPTER_PAGE, block = block)
        }

        override fun parse(): NovelDetail = _novelDetail.createNovelDetail()

        private inner class _NovelDetail {
            var novel: NovelItem? = null
            var image: String? = null
            var update: Date? = null
            var introduction: String? = null
            var extra: String? = null
            fun createNovelDetail() = NovelDetail(
                    requireNotNull(novel),
                    requireNotNull(image),
                    update,
                    introduction.toString(),
                    requireNotNull(extra)
            )
        }
    }

    /*
       *************** chapters ***************
        */

    override fun getNovelChaptersAsc(extra: String): List<NovelChapter> =
            _Chapters(extra).initChapters(extra)

    private lateinit var initChapters: _Chapters.(String) -> List<NovelChapter>
    protected fun chapters(init: _Chapters.(String) -> List<NovelChapter>) {
        initChapters = init
    }

    protected inner class _Chapters(extra: String) : _Requester(extra) {
        fun document(
                document: Document = parse(connection
                        ?: connect(getNovelChapterUrl(extra)), charset),
                init: _NovelChapterListParser.() -> Unit
        ): List<NovelChapter> =
                _NovelChapterListParser(document).also(init).parse()
    }

    protected inner class _NovelChapterListParser(root: Element)
        : _Parser<List<NovelChapter>>(root) {
        private lateinit var novelChapterList: List<NovelChapter>

        @SuppressWarnings("SimpleDateFormat")
        fun lastUpdate(query: String, parent: Element = root, format: String, block: (Element) -> String = { it.text() }) =
                lastUpdate(query = query, parent = parent) {
                    val updateString = block(it)
                    val sdf = SimpleDateFormat(format)
                    sdf.parse(updateString)
                }

        fun lastUpdate(query: String, parent: Element = root, block: (Element) -> Date) {
            novelChapterList.last().update = parent.getElement(query = query, block = block)
        }

        fun items(query: String, parent: Element = root, init: _NovelChapterParser.() -> Unit = {
            name = root.text()
            // 默认从该元素的href路径中找到chapterId，用于拼接章节正文地址，
            extra = findChapterId(root.path())
        }) {
            novelChapterList = parent.requireElements(query, name = TAG_CHAPTER_LINK).mapNotNull {
                // 解析不通过的章节直接无视，不抛异常，
                tryOrNul {
                    _NovelChapterParser(it).run {
                        init()
                        parse()
                    }
                }
            }
        }

        override fun parse(): List<NovelChapter> = novelChapterList
    }

    protected inner class _NovelChapterParser(root: Element)
        : _Parser<NovelChapter>(root) {
        private val _novelChapter = _NovelChapter()
        override fun parse(): NovelChapter? = tryOrNul {
            _novelChapter.createNovelChapter()
        }

        var name: String?
            get() = _novelChapter.name
            set(value) {
                _novelChapter.name = value
            }

        fun name(query: String, parent: Element = root, block: (Element) -> String = { it.text() }) {
            name = parent.requireElement(query, block = block)
        }

        var extra: String?
            get() = _novelChapter.extra
            set(value) {
                _novelChapter.extra = value
            }

        fun extra(query: String, parent: Element = root, block: (Element) -> String = { it.absHref() }) {
            extra = parent.requireElement(query, block = block)
        }

        var update: Date?
            get() = _novelChapter.update
            set(value) {
                _novelChapter.update = value
            }

        @SuppressWarnings("SimpleDateFormat")
        fun update(query: String, parent: Element = root, format: String, block: (Element) -> String = { it.text() }) =
                update(query = query, parent = parent) {
                    val updateString = block(it)
                    val sdf = SimpleDateFormat(format)
                    sdf.parse(updateString)
                }

        fun update(query: String, parent: Element = root, block: (Element) -> Date) {
            update = parent.getElement(query = query, block = block)
        }

        private inner class _NovelChapter {
            var name: String? = null
            var extra: String? = null
            var update: Date? = null
            fun createNovelChapter(): NovelChapter = NovelChapter(
                    name = requireNotNull(name),
                    extra = requireNotNull(extra),
                    update = update
            )
        }
    }

    override fun getNovelContent(extra: String): List<String> =
            _Content(extra).initContent(extra)

    private lateinit var initContent: _Content.(String) -> List<String>
    protected fun content(init: _Content.(String) -> List<String>) {
        initContent = init
    }

    protected inner class _Content(extra: String) : _Requester(extra) {
        fun document(
                document: Document = parse(connection
                        ?: connect(getNovelContentUrl(extra)), charset),
                init: _NovelContentParser.() -> Unit
        ): List<String> = _NovelContentParser(document).also(init).parse()
    }

    protected inner class _NovelContentParser(root: Element)
        : _Parser<List<String>>(root) {
        private lateinit var novelContent: List<String>
        // 查到的可以是一个元素，也可以是一列元素，
        fun items(query: String, parent: Element = root, block: (Element) -> List<String> = { it.textList() }) {
            novelContent = parent.requireElements(query, name = TAG_CONTENT).flatMap {
                block(it)
            }
        }

        override fun parse(): List<String> = novelContent
    }

    /*
    *************** novel ***************
     */

    protected inner class _NovelItemParser(root: Element) : _Parser<NovelItem>(root) {
        private val _novelItem = _NovelItem()
        var name: String?
            get() = _novelItem.name
            set(value) {
                _novelItem.name = value
            }

        fun name(query: String, parent: Element = root, block: (Element) -> String = { it.text() }) {
            name = parent.requireElement(query = query, name = TAG_NOVEL_NAME) {
                // 为了从列表中拿小说时方便，
                // 尝试从该元素中提取bookId，如果能成功，就不需要调用extra块，
                // 如果是详情页，在这前后传入详情页的extra都可以，不会被这里覆盖，
                if (_novelItem.extra == null && it.href().isNotBlank()) {
                    _novelItem.extra = findBookId(it.path())
                }
                block(it)
            }
        }

        var author: String?
            get() = _novelItem.author
            set(value) {
                _novelItem.author = value
            }

        fun author(query: String, parent: Element = root, block: (Element) -> String = { it.text() }) {
            author = parent.requireElement(query = query, name = TAG_AUTHOR_NAME, block = block)
        }

        var extra: String?
            get() = _novelItem.extra
            set(value) {
                _novelItem.extra = value
            }

        fun extra(query: String, parent: Element = root, block: (Element) -> String = { findBookId(it.path()) }) {
            extra = parent.requireElement(query = query, name = TAG_NOVEL_LINK, block = block)
        }

        override fun parse(): NovelItem = _novelItem.createNovelItem()

        private inner class _NovelItem {
            var site: String = this@DslJsoupNovelContext.site.name
            var name: String? = null
            var author: String? = null
            // extra不能给初值，搜索结果页面要有个自动判断填充extra的逻辑，
            var extra: String? = null

            fun createNovelItem() = NovelItem(
                    site,
                    requireNotNull(name),
                    requireNotNull(author),
                    requireNotNull(extra)
            )
        }
    }

    /*
    *************** parser ***************
     */
    @DslTag
    protected abstract inner class _Parser<out T>(
            val root: Element
    ) {
        fun element(query: String, parent: Element = root) =
                parent.requireElement(query)

        abstract fun parse(): T?
    }

    /*
    *************** requester ***************
     */
    @DslTag
    protected abstract inner class _Requester(
            protected val extra: String
    ) {
        var connection: Connection? = null
        // 指定响应的编码，用于jsoup解析html时，
        // 不是参数的编码，参数不进行额外的URLEncode,
        var charset: String? = this@DslJsoupNovelContext.charset

        fun get(init: _Request.() -> Unit) {
            connection = _Request().run {
                method = Connection.Method.GET
                init()
                createConnection()
            }
        }

        fun post(init: _Request.() -> Unit) {
            connection = _Request().run {
                method = Connection.Method.POST
                init()
                createConnection()
            }
        }

        fun <T> response(block: (String) -> T): T {
            val body = response(requireNotNull(connection).ignoreContentType(true)).body()
            return block(body)
        }
    }

    @DslTag
    protected inner class _Request {
        var url: String? = null
        var charset: String? = this@DslJsoupNovelContext.charset
        var method: Connection.Method? = null
        var dataMap: Map<String, String>? = null
        fun createConnection(): Connection = connect(absUrl(requireNotNull(url)))
                .method(requireNotNull(method))
                .apply { dataMap?.also { data(it) } }
                .apply { charset?.also { postDataCharset(it) } }

        /**
         * TODO: 这种方法装载参数的话，如果是get, jsoup写死URLEncode编码utf-8, 需要用到的话就整个改okhttp吧，
         */
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

    /*
    *************** annotation ***************
     */
    @DslMarker
    annotation class DslTag

    /*
    *************** url ***************
     */
    override fun getNovelContentUrl(extra: String): String {
        return if (::getNovelContentUrlLambda.isInitialized) {
            absUrl(getNovelContentUrlLambda(extra))
        } else {
            super.getNovelContentUrl(extra)
        }
    }

    private lateinit var getNovelContentUrlLambda: (String) -> String
    fun getNovelContentUrl(lambda: (String) -> String) {
        getNovelContentUrlLambda = lambda
    }
}
