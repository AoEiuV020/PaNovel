package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.base.jar.findAll
import cc.aoeiuv020.base.jar.pick
import cc.aoeiuv020.base.jar.textList
import net.sf.jazzlib.ZipFile
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.domain.TOCReference
import nl.siegmann.epublib.epub.EpubReader
import org.jsoup.Jsoup
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
    // jar协议最后的斜杆/是必须的，拼接的时候不要重复了，
    private val url = URL("jar:${file.toURI()}!/")

    @Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER")
    override fun parse(): LocalNovelInfo {
        var author: String? = null
        var name: String? = null
        var image: String? = null
        var introduction: String? = null
        val chapters: MutableList<LocalNovelChapter> = LinkedList()
        // epub也需要指定编码，
        val requester: String = charset.name()

        val book: Book = EpubReader().readEpubLazy(ZipFile(file), charset.name())

        // 如果只有lastName, 会得到空格开头，
        author = book.metadata?.authors?.firstOrNull()?.run { "$firstname $lastname" }?.trim()
        // 一般来说都是存在第一个非空白title的，
        name = book.metadata?.titles?.firstOrNull { it.isNotBlank() }?.trim()
        // 封面好像有可能只有coverPage没有coverImage，不管，无视，空安全就好，
        // 封面只保存相对路径，这样解析临时文件后移动临时文件依然可用，
        image = book.coverImage?.href
        // descriptions中存的一般是html,
        val introductionInDescriptions = book.metadata?.descriptions?.flatMap {
            Jsoup.parse(it).body().textList()
        } ?: listOf()
        // 爱下电子书网站下载的epub有在封面页面放简介，
        val introductionInCoverPage = book.coverPage?.inputStream?.use { input ->
            Jsoup.parse(input, charset.name(), url.toString()).findAll {
                /*
                <li xmlns="http://www.w3.org/1999/xhtml">『内容简介：妖魔中的至高无上者，名为“大圣”。    』</li>
                 */
                it.ownText().contains("内容简介：")
            }.map {
                it.ownText().pick("内容简介：([^』]*)(』)?").first().trim()
            }
        } ?: listOf()
        introduction = (introductionInDescriptions + introductionInCoverPage).joinToString("\n")

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
                chapters.add(LocalNovelChapter(name = title, extra = href))
            }
            toc.children.forEach(::addToc)
        }

        // tableOfContents不一定包括了封面，或者说，爱下电子书网站的epub和tableOfContents不包括封面，
        book.tableOfContents.tocReferences.forEach(::addToc)

        // 加上封面，
        book.coverPage?.let {
            val title = it.title ?: it.inputStream.use { input ->
                Jsoup.parse(input, charset.name(), url.toString())
                        // jsoup的title可能empty, 不可能null,
                        .title().takeIf { it.isNotBlank() }?.trim()
            } ?: "封面"
            LocalNovelChapter(name = title, extra = it.href)
        }?.let { cover ->
            // 如果第一个章节不是封面，才加上封面，
            if (chapters.firstOrNull()?.extra != cover.extra) {
                chapters.add(0, cover)
            }
        }

        return LocalNovelInfo(author, name, image, introduction, chapters, requester)
    }

    // 章节内容开头可能是章节名，不过滤了，完整读取epub中的章节内容，改成展示前对比过滤一下，
    override fun getNovelContent(extra: String): List<String> {
        val chapter = URL(url, extra)
        return chapter.openStream().use { input ->
            // 本章地址做baseUri, 才能正确得到引用的资源，比如图片，
            // ![img](jar:file:/home/aoeiuv/tmp/panovel/epub/打工吧！魔王大人17.epub!/OPS/images/17-007.jpg)
            Jsoup.parse(input, charset.name(), chapter.toString())
        }.body().textList()
        // 爱下电子书网站的广告，不想过滤，epub就应该完整保留，
/*
                .dropLastWhile {
                    it == "书迷楼最快更新，无弹窗阅读请收藏书迷楼(.com)。"
                            || it.all { it == '=' }
                            || it.startsWith("手机访问:https?://")
                            || it.startsWith("关注更新请访问:https?://")
                            || it == "『还在连载中...』"
                }
*/
    }

    override fun getCoverImage(extra: String): URL {
        return URL(url, extra)
    }
}
