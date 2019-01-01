package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.base.jar.debug
import cc.aoeiuv020.base.jar.findAll
import cc.aoeiuv020.base.jar.textList
import cc.aoeiuv020.regex.pick
import net.sf.jazzlib.ZipFile
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.domain.TOCReference
import nl.siegmann.epublib.epub.EpubReader
import org.jsoup.Jsoup
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URL
import java.nio.charset.Charset
import java.util.*

/**
 * Created by AoEiuV020 on 2018.06.16-17:10:44.
 */
class EpubParser(
        // epub文件一定是zip压缩包，
        file: File,
        private val charset: Charset
) : LocalNovelParser(file) {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass.simpleName)
    // jar协议最后的斜杆/是必须的，拼接的时候不要重复了，
    private val rootUrl = URL("jar:${file.toURI()}!/")

    @Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER")
    override fun parse(): LocalNovelInfo {
        var author: String? = null
        var name: String? = null
        var image: String? = null
        var introduction: String? = null
        val chapters: MutableList<LocalNovelChapter> = LinkedList()
        // epub也需要指定编码，
        val requester: String = charset.name()

        val zipFile = ZipFile(file)
        // 这个编码好像没什么用，epublib库里到处都是写死的utf8,
        val book: Book = EpubReader().readEpubLazy(zipFile, charset.name())
        // 马上就可以关闭了，
        zipFile.close()

        val opfUrl = URL(rootUrl, book.opfResource.href)

        // 一般来说都是存在第一个非空白title的，
        name = book.metadata?.titles?.firstOrNull { it.isNotBlank() }?.trim()
        // 没有作者就null, 不要用name代替，用的时候再考虑这个，
        author = book.metadata?.authors?.firstOrNull()
                // 如果只有lastName, 会得到空格开头，
                ?.run { "$firstname $lastname" }
                // 没有作者就null，不要空字符串，
                ?.takeIf(String::isNotBlank)?.trim()
        // 封面好像有可能只有coverPage没有coverImage，不管，无视，空安全就好，
        // 封面只保存相对路径，这样解析临时文件后移动临时文件依然可用，
        image = book.coverImage?.href?.let {
            getPath(opfUrl, it)
        }
        // descriptions中存的一般是html,
        val introductionInDescriptions = book.metadata?.descriptions?.flatMap {
            // 不按空格分割，否则英文电子书就很糟糕了，
            Jsoup.parse(it).body().textList()
        } ?: listOf()
        // 爱下电子书网站下载的epub有在封面页面放简介，
        val introductionInCoverPage = book.coverPage?.inputStream?.use { input ->
            Jsoup.parse(input, charset.name(), rootUrl.toString()).findAll {
                /*
                <li xmlns="http://www.w3.org/1999/xhtml">『内容简介：妖魔中的至高无上者，名为“大圣”。    』</li>
                 */
                it.ownText().contains("内容简介：")
            }.map {
                it.ownText().pick("内容简介：([^』]*)(』)?").first().trim()
            }
        } ?: listOf()
        introduction = (introductionInDescriptions + introductionInCoverPage).joinToString("\n")
                // 没有简介就null，不要空字符串，
                .takeIf(String::isNotBlank)

        // 深度优先遍历，
        // 虽然TOCReference有个方法allUniqueResources能深度优先遍历拿reference, 但是丢了TOCReference就拿不到title章节名了，
        // 不去重，按理说href不会重复，章节也不会重复出现，
        fun addToc(toc: TOCReference) {
            // 按理说是有title章节名的，
            val title = toc.title ?: "unknown"
            // 按理说href不会为空，
            // 缺测试样例，不知道如果是没有内容的卷名，会不会没有href,
            val href: String? = toc.resource?.href
            // 分开判断才能smart case,
            if (href != null && href.isNotBlank()) {
                // 目录href记录的是相对opf文件的路径，
                // 我要的是相对根目录的路径，
                val extra = getPath(opfUrl, href)
                logger.debug {
                    "add chapter: name=$title, extra=$extra"
                }
                chapters.add(LocalNovelChapter(name = title, extra = extra))
            }
            toc.children.forEach(::addToc)
        }

        // tableOfContents不一定包括了封面，或者说，爱下电子书网站的epub和tableOfContents不包括封面，
        book.tableOfContents.tocReferences.forEach(::addToc)

        // 加上封面，
        book.coverPage?.let {
            val title = it.title ?: it.inputStream.use { input ->
                Jsoup.parse(input, charset.name(), rootUrl.toString())
                        // jsoup的title可能empty, 不可能null,
                        .title().takeIf { it.isNotBlank() }?.trim()
            } ?: "封面"
            LocalNovelChapter(name = title, extra = getPath(opfUrl, it.href))
        }?.let { cover ->
            // 如果第一个章节不是封面，才加上封面，
            if (chapters.firstOrNull()?.extra != cover.extra) {
                chapters.add(0, cover)
            }
        }

        return LocalNovelInfo(author, name, image, introduction, chapters, requester)
    }

    override fun getNovelContent(chapter: LocalNovelChapter): List<String> {
        val extra = chapter.extra
        val chapterUrl = URL(rootUrl, extra)
        logger.debug { "getContent: $chapterUrl" }
        return chapterUrl.openStream().use { input ->
            // 本章地址作为baseUri, 才能正确得到引用的资源，比如图片，
            // ![img](jar:file:/home/aoeiuv/tmp/panovel/epub/打工吧！魔王大人17.epub!/OPS/images/17-007.jpg)
            Jsoup.parse(input, charset.name(), chapterUrl.toString())
        }.body().textList()
                // 章节内容开头可能是章节名，过滤掉，正文不包括章节名，
                .dropWhile { it == chapter.name }
    }

    // 封面extra只有包内路径，要展开成完整jar协议地址，
    override fun getImage(extra: String): URL {
        // api19模拟器上测试发现文件路径会重复出现，电脑和高版本安卓没有出现，
        return URL(rootUrl, extra.removePrefix(rootUrl.toString())).also {
            logger.debug { "getImage <$extra> -> <$it>" }
        }
    }

    /**
     * URL识别的jar协议的path是包括文件路径的，
     * 但是计算spec又能正确处理包内路径，
     * 所以要删除path开头和根目录开关一致的部分，
     */
    private fun getPath(base: URL, href: String): String {
        return URL(base, href).path.removePrefix(rootUrl.path)
    }
}
