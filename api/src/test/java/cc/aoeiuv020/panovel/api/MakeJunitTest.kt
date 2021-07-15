package cc.aoeiuv020.panovel.api

import cc.aoeiuv020.panovel.api.site.BaseNovelContextText
import cc.aoeiuv020.panovel.api.site.N69shu
import org.junit.Test
import java.io.File
import java.util.*

/**
 * 自动生成测试样例，
 * Created by AoEiuV020 on 2021.07.15-22:03:55.
 */
class MakeJunitTest : BaseNovelContextText(
    N69shu::class
) {
    @Suppress("RemoveExplicitTypeArguments")
    private val specialBookList = listOf<String>(
        "柯南里的捡尸人"
    )
    private val testBookList = mutableListOf<NovelItem>()
    private val testChapterList = mutableListOf<NovelChapter>()

    private fun makeSearch(): String {
        val key = "都市"
        println("生成通用的${key}的搜索")
        val sb = StringBuilder()
        sb.append(
            """
        search("$key")
""".trimStart('\n')
        )

        println("生成一个精确搜索")
        val result = site.searchNovelName(key).first()
        testBookList.add(result)
        sb.append(
            """
        search("${result.name}", "${result.author}", "${result.extra}")
""".trimStart('\n')
        )

        println("生成需要特地测试的小说的搜索")
        for (name in specialBookList) {
            val testResult = site.searchNovelName(name).first()
            testBookList.add(testResult)
            sb.append(
                """
        search("${testResult.name}", "${testResult.author}", "${testResult.extra}")
""".trimStart('\n')
            )
        }
        return sb.toString().trimEnd()
    }

    private fun makeDetail(): String {
        val sb = StringBuilder()
        for (item in testBookList) {
            val detail = site.getNovelDetail(item.extra)
            sb.append(
                """
        detail(
            "%s", "%s", "%s", "%s",
            %s,
            %s,
            %s
        )
""".trimStart('\n').format(
                    item.extra, detail.extra, detail.novel.name, detail.novel.author,
                    quote(detail.image),
                    quote(detail.introduction),
                    quote(dateString(detail.update))
                )
            )
        }
        return sb.toString().trimEnd()
    }

    private fun dateString(update: Date?): String? {
        if (update == null) {
            return null
        }
        return sdf.format(update)
    }

    private fun quote(str: String?): String {
        if (str == null) {
            return "null"
        }
        return escapeForJava(str)
    }

    private fun escapeForJava(value: String, quote: Boolean = true): String {
        val builder = java.lang.StringBuilder()
        if (quote) builder.append("\"")
        for (c in value.toCharArray()) {
            when (c) {
                '\'' -> builder.append("\\'")
                '\"' -> builder.append("\\\"")
                '\r' -> builder.append("\\r")
                '\n' -> builder.append("\\n")
                else -> builder.append(c)
            }
        }
        if (quote) builder.append("\"")
        return builder.toString()
    }

    private fun makeChapters(): String {
        val sb = StringBuilder()
        for (item in testBookList) {
            val chapters = site.getNovelChaptersAsc(item.extra)
            val first = chapters.first()
            val last = chapters.last()
            testChapterList.add(last)
            sb.append(
                """
        chapters(
            "%s", "%s", "%s", %s,
            "%s", "%s", %s,
            %d
        )
""".trimStart('\n').format(
                    item.extra, first.name, first.extra, quote(dateString(first.update)),
                    last.name, last.extra, quote(dateString(last.update)),
                    chapters.size
                )
            )
        }
        return sb.toString().trimEnd()
    }

    private fun makeContent(): String {
        val sb = StringBuilder()
        for (chapter in testChapterList) {
            val content = site.getNovelContent(chapter.extra)
            sb.append(
                """
        content(
            "%s",
            %s,
            %s,
            %d
        )
""".trimStart('\n').format(
                    chapter.extra,
                    quote(content.first()),
                    quote(content.last()),
                    content.size
                )
            )
        }
        return sb.toString().trimEnd()
    }

    @Test
    fun generator() {
        val testFile = File(
            "src/test/java/" +
                    this.javaClass.packageName.replace('.', '/') +
                    "/site/${site.javaClass.simpleName}Test.kt"
        ).absoluteFile
        template.format(
            this.javaClass.packageName,
            site.javaClass.simpleName,
            site.javaClass.simpleName,
            makeSearch(),
            makeDetail(),
            makeChapters(),
            makeContent()
        ).let {
            testFile.writeText(it)
        }
    }

    private val template: String = """
package %s.site

import org.junit.Test

class %sTest : BaseNovelContextText(%s::class) {
    @Test
    fun search() {
%s
    }

    @Test
    fun detail() {
%s
    }

    @Test
    fun chapters() {
%s
    }

    @Test
    fun content() {
%s
    }

}
    """.trimIndent()
}