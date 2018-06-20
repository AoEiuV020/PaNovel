package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.base.jar.pick
import java.io.File
import java.net.URL

/**
 * Created by AoEiuV020 on 2018.06.19-22:17:27.
 */
class TextExporter(
        private val file: File
) : LocalNovelExporter {
    private val imagePattern = "^!\\[img\\]\\((.*)\\)$"
    override fun export(info: LocalNovelInfo, contentProvider: ContentProvider, progressCallback: (Int, Int) -> Unit) {
        file.outputStream().bufferedWriter().use { output ->

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
                URL(info.image).takeIf { it.isHttp() }?.let {
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
                // 章节之间空两行，
                // 第一章前也空了两行，和小说信息分开，
                output.appendln()
                output.appendln()
                output.appendln(chapter.name)

                contentProvider.getNovelContent(chapter.extra).forEach {
                    try {
                        // 是图片要判断一下是否是网络图片，是就保存，否则过滤，
                        // TODO: 考虑改成从ContentProvider拿图片URL,
                        if (URL(it.pick(imagePattern).first()).isHttp()) {
                            // 也要缩进，否则会被当成一章，
                            output.appendln("$intent$it")
                        } else {
                            // 是图片但不是网络图片就直接跳过，
                        }
                    } catch (e: Exception) {
                        // 不是图片就直接保存，
                        output.appendln("$intent$it")
                    }
                }

                progressCallback(index, total)
            }
            // 导出结束，必要的，index比size小1,
            progressCallback(total, total)
            // 以防万一，关闭前刷个缓冲，
            output.flush()
        }
    }

    private fun URL.isHttp() = protocol in listOf("http", "https")
}