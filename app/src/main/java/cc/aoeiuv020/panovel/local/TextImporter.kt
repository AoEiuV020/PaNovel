package cc.aoeiuv020.panovel.local

import android.content.Context
import cc.aoeiuv020.base.jar.notNull
import cc.aoeiuv020.irondb.Database
import cc.aoeiuv020.irondb.delegate
import cc.aoeiuv020.irondb.read
import cc.aoeiuv020.irondb.write
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.data.entity.Novel
import java.io.InputStream
import java.nio.charset.Charset
import java.util.*

/**
 * 负责导入本地小说的，
 *
 * 一次只处理一本，
 * 导入分两步，
 * 先处理后存在"/import"目录下，
 * 再考虑作者名小说名之类的插入数据库后再从"/import"移到统一的缓存中，
 *
 * Created by AoEiuV020 on 2018.06.12-14:17:19.
 */
class TextImporter constructor(
        private val root: Database
) {
    private val contentTable = root.sub(KEY_CONTENT)
    var author: String? by root.delegate()
    var name: String? by root.delegate()
    // 简介也是和正文一样，一段一段的，
    var introduction: List<String>? by root.delegate()
    // 处理后一定有要章节列表，
    var chapters: List<NovelChapter>? by root.delegate()

    /**
     * 导入普通.txt格式小说，
     * 按“知轩藏书”的.txt格式解析，
     *
     * input在外面关闭，
     */
    fun importText(input: InputStream, charset: Charset) {
        // 整个目录是一次性的，以防万一，前后都清一波，
        clean()
        var index = 0
        // 考虑到频繁add以及最后有remove首尾的操作，用链表，
        val chapters = LinkedList<NovelChapter>()
        val content = LinkedList<String>()
        input.reader(charset).forEachLine { line ->
            if (false == line.firstOrNull()?.isWhitespace()) {
                // 行开头不是空格的都当成章节名，
                // 第一个章节之前的内容存为0,
                // 可能空列表，允许，卷名也单独一章空列表，
                contentTable.write(index.toString(), content)
                content.clear()
                ++index
                chapters.add(NovelChapter(line, extra = index.toString()))
            } else if (line.isNotBlank()) {
                // 空行无视，
                content.add(line.trim())
            }
        }
        // 最后一章也要写，
        if (content.isNotEmpty()) {
            contentTable.write(index.toString(), content)
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
        val infoLinesCount = chapters.take(10).indexOfLast {
            it.name.startsWith("作者：")
                    || it.name == "内容简介"
        } + 1
        // 如果找不到，take(0),后面遍历什么都不做，
        val infoList = chapters.take(infoLinesCount)
        // 删除开头的小说信息，
        repeat(infoLinesCount) {
            chapters.removeAt(0)
        }

        // 保存结果，
        this.chapters = chapters
        infoList.forEach {
            when {
                it.name.startsWith("作者：") -> this.author = it.name.removePrefix("作者：")
                it.name == "内容简介" -> this.introduction = contentTable.read(it.extra)
            // 小说名只赋值一次，
                this.name == null -> this.name = it.name
            }
        }
    }

    fun getContent(extra: String): List<String> {
        return contentTable.read<List<String>>(extra).notNull()
    }

    fun clean() {
        root.drop()
    }

    fun exportText(ctx: Context, novel: Novel) {
        TextExporter.export(ctx, novel)
    }

    companion object {
        const val KEY_LOCAL = "local"
        const val KEY_CONTENT = "content"
    }
}
