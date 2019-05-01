package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.regex.pick
import java.io.File
import java.net.URL
import java.nio.charset.Charset

/**
 * Created by AoEiuV020 on 2018.06.19-22:17:27.
 */
class TextExporter(
        private val file: File,
        private val charset: String
) : LocalNovelExporter {
    private val imagePattern = "^!\\[img\\]\\((.*)\\)$"
    override fun export(info: LocalNovelInfo, contentProvider: ContentProvider, progressCallback: (Int, Int) -> Unit) {
        file.outputStream().bufferedWriter(Charset.forName(charset)).use { output ->

            val chapters = info.chapters
            val total = chapters.size
            // 段首缩进不能没有，否则影响导入，
            // contentProvider提供的正文行都不带缩进，
            val intent = "　　"

            progressCallback(0, total)

            // 先导出小说信息，包括小说名，作者名，封面，顶格输出，
            output.appendln(info.name)
            output.appendln("作者：${info.author}")
            try {
                // 只保留网络连接协议的封面，这样的封面换个设备还能用，
                info.image?.let { contentProvider.getImage(it) }
                        ?.takeIf { it.isHttp() }?.let {
                            output.appendln("封面：$it")
                        }
            } catch (_: Exception) {
                // 无视任何错误，大不了不添加封面，
            }
            // 简介前空一行，
            output.appendln()
            output.appendln("内容简介")
            info.introduction?.split("\n")?.forEach {
                // 简介内容段首空格，当成一章，
                output.appendln("$intent$it")
            }

            chapters.forEachIndexed { index, chapter ->
                progressCallback(index, total)
                val content = contentProvider.getNovelContent(chapter)
                // 空章节不导出，
                if (content.isEmpty()) return@forEachIndexed

                // 章节之间空两行，
                // 第一章前也空了两行，和小说信息分开，
                output.appendln()
                output.appendln()
                output.appendln(chapter.name)

                content.forEach {
                    try {
                        // 是图片要判断一下是否是网络图片，是就保存，否则过滤，
                        contentProvider.getImage(it.pick(imagePattern).first()).takeIf { it.isHttp() }
                                // 也要缩进，否则会被当成一章，
                                ?.let { output.appendln("$intent$it") }
                        // 是图片但不是网络图片就留个单词image表示这里有张图片，
                                ?: output.appendln("$intent[image]")
                    } catch (e: Exception) {
                        // 不是图片就直接保存，
                        output.appendln("$intent$it")
                    }
                }

            }
            // 导出结束，必要的，index比size小1,
            progressCallback(total, total)
            // 以防万一，关闭前刷个缓冲，
            output.flush()
        }
    }

    private fun URL.isHttp() = protocol in listOf("http", "https")
}