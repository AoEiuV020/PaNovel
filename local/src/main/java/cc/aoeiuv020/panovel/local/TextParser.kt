package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.base.jar.debug
import cc.aoeiuv020.base.jar.divide
import cc.aoeiuv020.base.jar.io.BufferedRandomAccessFile
import cc.aoeiuv020.base.jar.io.readLines
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.charset.Charset
import java.util.*

/**
 * 解析纯文本小说，得到每一章节对应的位置，
 *
 * Created by AoEiuV020 on 2018.06.13-16:50:06.
 */
class TextParser(
        file: File,
        private val charset: Charset
) : LocalNovelParser(file) {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass.simpleName)

    override fun getNovelContent(chapter: LocalNovelChapter): List<String> {
        val extra = chapter.extra
        val (beginPos, endPos) = extra.divide('/').let {
            it.first.toLong() to it.second.toLong()
        }
        return BufferedRandomAccessFile(file, "r").use { raf ->
            // map去掉段首空格，顺便转成随机访问的ArrayList,
            raf.readLines(beginPos, endPos, charset.name()).map {
                it.trim()
            }
                    // 章节内容开头可能是章节名，过滤掉，正文不包括章节名，
                    .dropWhile { it == chapter.name }

        }
    }

    override fun parse(): LocalNovelInfo {
        var author: String? = null
        var name: String? = null
        var image: String? = null
        var introduction: String? = null
        // 考虑到频繁add以及最后有remove首尾的操作，用链表，事后转成api模块的NovelChapter时顺便转成ArrayList,
        val chapters: MutableList<LocalNovelChapter> = LinkedList()
        val requester: String = charset.name()
        // 用两个输入流实属无奈，除了要记录文件指针就只能处理byte流，但是bytes转String太费时，而且是只有非默认编码费时，原因不明，
        // 两个输入流加速效果，从 37s 到 14s,
        BufferedRandomAccessFile(file, "r").use { raf ->
            var beginPos = 0L
            var endPos = 0L
            var chapterName: String? = null
            file.inputStream().reader(charset).buffered().forEachLine { line ->
                raf.skipLine()
                if (false == line.firstOrNull()?.isWhitespace()) {
                    logger.debug {
                        "add chapter: chapterName=$chapterName, extra=$beginPos/$endPos"
                    }
                    // 行开头不是空格的都当成章节名，事后再过滤小说信息和广告，
                    // 第一个章节之前的内容存为0,
                    // 可能空内容，允许，卷名也单独一章空列表，

                    // 开始新章节前，把之前的章节名和内容插入列表中，
                    if (chapterName != null) {
                        // 第一个章节名出现前的正文通通无视，
                        // 保存章节信息，extra存两个指针位置，
                        // 以防万一，不能让endPos比beginPos大，
                        endPos = maxOf(beginPos, endPos)
                        chapters.add(LocalNovelChapter(chapterName!!, extra = "$beginPos/$endPos"))
                    }
                    chapterName = line
                    // 记录章节名所在行后的位置，作为章节内容的开始，
                    beginPos = raf.filePointer
                    // 以防万一，空内容的话可能不走下面的else, 不能让endPos比beginPos大，
                    endPos = beginPos
                } else if (line.isNotBlank()) {
                    // 空行无视，
                    // 记录该行后的位置，作为章节内容的结尾，
                    // 有下一行会继续走这里，继续延后endPos,
                    endPos = raf.filePointer
                }
            }
            // 最后一章也要存，
            if (chapterName != null) {
                // 第一个章节名出现前的正文通通无视，
                // 保存章节信息，extra存两个指针位置，
                endPos = maxOf(beginPos, endPos)
                chapters.add(LocalNovelChapter(chapterName!!, extra = "$beginPos/$endPos"))
            }
        }
        // chapters中有很多没用的，比如广告链接，开头可能存在的小说名，作者名，
        if (!chapters.isEmpty()) {
            // 删除知轩藏书的广告，
            // 开头和结尾有固定文字，
            /*
            ==========================================================
            更多精校小说尽在知轩藏书下载：http://www.zxcs8.com/
            ==========================================================
             */
            // dropWhile,
            val reversedIte = chapters.listIterator(chapters.size)
            while (reversedIte.hasPrevious()) {
                val chapter = reversedIte.previous().name
                if (chapter.startsWith("更多精校小说尽在知轩藏书下载：")
                        || chapter.all { it == '=' }) {
                    reversedIte.remove()
                }
            }
            // dropLastWhile,
            val ite = chapters.iterator()
            while (ite.hasNext()) {
                val chapter = ite.next().name
                if (chapter.startsWith("更多精校小说尽在知轩藏书下载：")
                        || chapter.all { it == '=' }) {
                    ite.remove()
                }
            }
        }
        /*
        与千年女鬼同居的日子
        作者：卜非

        内容简介
        　　为了赚点零花钱代人扫墓，结果一只女鬼跟着回了家，额滴个神呀，从此诡异的事情接二连三的发生在了自己身边。
        　　红衣夜女杀人案、枯井中的无脸之人、河中的人形怪物……
        　　更为奇怪的是，那些平时连想都不敢想的女神都主动凑了过来。
        　　虽然这只女鬼长得俊俏又漂亮，可等知道她的真正身份之后，我和我的小伙伴顿时都惊呆了。
         */
        // 找可能存在正文之前的作者信息和小说简介信息，
        // 固定找十个，找不到就算了，找到就提出来然后删除，
        // 知轩藏书的格式为，0:小说名，1:作者名, 2:内容简介，
        // 最后一个符合的章节也要包括，
        // 返回的是最后一个符合条件的章节的索引，
        // 加一成为行数，
        // 找不到返回 -1 + 1 == 0, 不影响后面take,
        val inesCount = chapters.take(10).indexOfLast {
            it.name.startsWith("作者：")
                    || it.name.startsWith("封面：")
                    || it.name == "内容简介"
        } + 1
        // 如果找不到，take(0),后面遍历什么都不做，
        val ist = chapters.take(inesCount)
        // 删除开头的小说信息，
        repeat(inesCount) {
            chapters.removeAt(0)
        }

        ist.forEach {
            when {
                it.name.startsWith("作者：") -> author = it.name.removePrefix("作者：")
            // 封面这个不是知轩藏书有的格式，自己app导出的有，
                it.name.startsWith("封面：") -> image = it.name.removePrefix("封面：")
                it.name == "内容简介" -> {
                    val (beginPos, endPos) = it.extra.divide('/').let {
                        it.first.toLong() to it.second.toLong()
                    }
                    BufferedRandomAccessFile(file, "r").use { raf ->
                        introduction = raf.readLines(beginPos, endPos, charset.name()).joinToString("\n") {
                            it.trim()
                        }
                    }
                }
            // 小说名只赋值一次，
                name == null -> name = it.name
            }
        }
        return LocalNovelInfo(author, name, image, introduction, chapters, requester)
    }
}