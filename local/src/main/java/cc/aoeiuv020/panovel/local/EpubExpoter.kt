package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.base.jar.debug
import cc.aoeiuv020.base.jar.divide
import cc.aoeiuv020.base.jar.error
import cc.aoeiuv020.base.jar.lastDivide
import nl.siegmann.epublib.domain.Author
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.domain.Resource
import nl.siegmann.epublib.domain.TOCReference
import nl.siegmann.epublib.epub.EpubWriter
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Created by AoEiuV020 on 2018.06.19-22:53:24.
 */
class EpubExporter(
        private val file: File
) : LocalNovelExporter {
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
            info.introduction?.also { intro ->
                try {
                    intro.split('\n').map(String::trim).filter(String::isNotEmpty)
                            .takeIf(List<*>::isNotEmpty)
                            ?.fold(Element("div")) { div, it ->
                                // 没考虑简介有图片的情况，
                                div.appendElement("p")
                                        .text("$indent$it")
                            }?.also { div ->
                                addDescription(div.outerHtml())
                            }
                } catch (e: Exception) {
                    // 按理说不会有失败，
                    logger.error(e) { "导出简介失败，" }
                }
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
                        // 失败填充无意义后辍的名字，
                        "cover.img"
                    }
                }
                val resource = url.openStream().use { input ->
                    Resource(input, "$IMAGE_PATH/$fileName")
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

        book.tableOfContents.apply {
            chapters.forEachIndexed { index, chapter ->
                val name = chapter.name
                val content = contentProvider.getNovelContent(chapter.extra)
                val fileName = "chapter_$index.html"
                val root = Document.createShell(("file://$OPS_PATH/$TEXT_PATH/$fileName"))
                val div = root.body().appendElement("div")
                content.forEach { line ->
                    div.appendElement("p")
                            .text("$indent$line")
                }
                val bytes = root.outerHtml().toByteArray(Charsets.UTF_8)
                val resource = Resource(bytes, "$TEXT_PATH/$fileName")
                book.addResource(resource)
                val toc = TOCReference(name, resource)
                addTOCReference(toc)
            }
        }

        // EpubWriter不带缓冲，加个缓冲，不确定有多大效果，
        file.outputStream().buffered().use { output ->
            EpubWriter().write(book, output)
        }
    }

    companion object {
        const val IMAGE_PATH = "Images"
        const val TEXT_PATH = "Text"
        const val OPS_PATH = "OEBPS"
    }
}