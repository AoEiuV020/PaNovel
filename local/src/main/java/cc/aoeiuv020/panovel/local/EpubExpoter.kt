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
import java.net.URL

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
                val url = contentProvider.getCoverImage(it)
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
            val name = chapter.name
            val content = contentProvider.getNovelContent(chapter.extra)
            val fileName = "chapter$index.html"
            val root = Document.createShell(("file://$OPS_PATH/$fileName"))
            val div = root.body().appendElement("div")
                    .text("")
            content.forEach { line ->
                try {
                    val extra = line.pick(imagePattern).first()
                    // TODO: 考虑改成从ContentProvider拿图片URL,
                    val url = URL(extra)
                    val resource = url.openStream().use { input ->
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
                    // 不是图片就直接保存p标签，
                    div.appendElement("p")
                            .text("$indent$line")
                }
            }
            val bytes = (root.outerHtml()).toByteArray(Charsets.UTF_8)
            val resource = Resource(bytes, fileName)
            book.addSection(name, resource)
            if (index == 0) {
                book.guide.coverPage = resource
            }
            progressCallback(index, total)
        }

        // EpubWriter不带缓冲，加个缓冲，不确定有多大效果，
        file.outputStream().buffered().use { output ->
            EpubWriter().write(book, output)
        }

        progressCallback(total, total)
    }

    companion object {
        const val OPS_PATH = "OEBPS"
    }
}