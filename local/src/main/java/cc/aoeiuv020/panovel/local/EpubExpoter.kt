package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.base.jar.*
import nl.siegmann.epublib.domain.Author
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.domain.Resource
import nl.siegmann.epublib.epub.EpubWriter
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileDescriptor

/**
 * TODO: 其他阅读器无法识别，
 *
 * Created by AoEiuV020 on 2018.06.19-22:53:24.
 */
class EpubExporter(
        private val file: File
) : LocalNovelExporter {
    private val imagePattern = "^!\\[img\\]\\((.*)\\)$"
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass.simpleName)
    override fun export(info: LocalNovelInfo, contentProvider: ContentProvider, progressCallback: (Int, Int) -> Unit) {
        val book = Book()
        val indent = "　　"

        val chapters = info.chapters
        val total = chapters.size

        progressCallback(0, total)

        book.metadata.apply {
            addPublisher("PaNovel")
            language = "zh"
            info.author?.also {
                try {
                    val (first, last) = it.divide(' ')
                    val firstName = first.trim()
                    val lastName = last.trim()
                    addAuthor(Author(firstName, lastName))
                    logger.debug { "添加作者<$firstName $lastName>" }
                } catch (e: Exception) {
                    addAuthor(Author(it))
                    logger.debug { "添加作者<$it>" }
                }
            }
            info.name?.also {
                addTitle(it)
                logger.debug { "添加标题<$it>" }
            }
            info.introduction?.let { intro ->
                try {
                    intro.split('\n').map(String::trim).filter(String::isNotEmpty)
                            .takeIf(List<*>::isNotEmpty)
                            ?.fold(Element("div")) { div, it ->
                                // 没考虑简介有图片的情况，
                                div.appendElement("p")
                                        .text("$indent$it")
                            }
                } catch (e: Exception) {
                    // 按理说不会有失败，
                    logger.error(e) { "导出简介失败，" }
                    null
                }
            }?.also { div ->
                addDescription(div.outerHtml())
            }
        }
        info.image?.let {
            try {
                val url = contentProvider.getImage(it)
                // info.image或者url中一般有图片文件名，
                // 但不可控，万一就出现重名的呢，
                val fileName = try {
                    it.lastDivide('/').second
                } catch (_: Exception) {
                    try {
                        url.toString().lastDivide('/').second
                    } catch (_: Exception) {
                        // 失败填充默认jpg一般没问题，
                        "cover.jpg"
                    }
                }
                val resource = url.openStream().use { input ->
                    Resource(input, fileName)
                }
                logger.debug {
                    "添加封面<$url, ${resource.href}>"
                }
                book.coverImage = resource
            } catch (e: Exception) {
                // 任何失败就不添加封面了，
                logger.error(e) { "导出封面失败" }
            }
        }

        var imageIndex = 0
        chapters.forEachIndexed { index, chapter ->
            progressCallback(index, total)
            val content = contentProvider.getNovelContent(chapter)
            // 空章节不导出，
            if (content.isEmpty()) return@forEachIndexed
            val name = chapter.name
            val fileName = "chapter$index.html"
            val root = Document.createShell(("file://$OPS_PATH/$fileName"))
            root.title(name)
            val div = root.body().appendElement("div")
                    .text("")
            // 如果第一行不是章节名，就添加一行章节名，
            // 正常正文应该是不包括章节名的，也就是会调用这个的，
            if (content.firstOrNull() != name) {
                div.appendElement("h2")
                        .text(name)
            }
            content.forEach { line ->
                val extra = try {
                    line.pick(imagePattern).first()
                } catch (_: Exception) {
                    // 不是图片就直接保存p标签，
                    div.appendElement("p")
                            .text("$indent$line")
                    return@forEach
                }
                try {
                    val url = contentProvider.getImage(extra)
                    // 如果打开图片输入流返回空，直接抛异常到下面的catch打印普通文本，
                    val resource = contentProvider.openImage(url).notNull().use { input ->
                        val suffix = try {
                            // 从url中拿文件后辍，
                            url.toString().lastDivide('.').second
                        } catch (e: Exception) {
                            // 失败填充默认jpg一般没问题，
                            "jpg"
                        }
                        Resource(input, "image${imageIndex++}.$suffix")
                    }
                    book.addResource(resource)
                    div.appendElement("p")
                            .appendElement("img")
                            .attr("src", resource.href)
                            // jsoup没有内容的标签不封闭，以防万一加上text就封闭了，
                            .text("")
                } catch (e: Exception) {
                    logger.error(e) { "导出图片<$extra>出错，" }
                    div.appendElement("p")
                            .text("$indent[image]")
                }
            }
            val bytes = (root.outerHtml()).toByteArray(Charsets.UTF_8)
            val resource = Resource(bytes, fileName)
            book.addSection(name, resource)
            // 这个guide不知道是否重要，
            if (index == 0) {
                book.guide.coverPage = resource
            }
        }

        // EpubWriter不带缓冲，加个缓冲，不确定有多大效果，
        // TODO: 这里use多余，write里面close了，
        file.outputStream()
                .also {
                    FileDescriptor::class.java.getDeclaredField("descriptor")
                            .apply { isAccessible = true }
                            .get(it.fd)
                            .toString()
                            .also { logger.info { "write fd: $it" } }
                }
                .buffered().use { output ->
            EpubWriter().write(book, output)
        }

        progressCallback(total, total)
    }

    companion object {
        const val OPS_PATH = "OEBPS"
    }
}