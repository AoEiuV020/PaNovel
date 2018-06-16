package cc.aoeiuv020.panovel.local

import org.junit.Assert
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.InputStream
import kotlin.reflect.KClass

/**
 * Created by AoEiuV020 on 2018.06.16-16:28:11.
 */
abstract class ParserTest(clazz: KClass<out LocalNovelParser>) {
    init {
        System.setProperty("org.slf4j.simpleLogger.log.${clazz.java.simpleName}", "trace")
    }

    @Rule
    @JvmField
    val folder = TemporaryFolder()

    protected fun getResource(resource: String): File? {
        val inputStream: InputStream? = javaClass.getResourceAsStream(resource)
        if (inputStream == null) {
            // 资源不存在就不测试了，
            println("warning: 资源<$resource>不存在，")
            return null
        }
        return inputStream.use { input ->
            getFile(input)
        }
    }

    protected fun getFile(path: String): File? {
        val file = File(path)
        if (!file.exists()) {
            // 文件不存在就不测试了，
            println("warning: 文件<$path>不存在，")
            return null
        }
        return file
    }

    private fun getFile(inputStream: InputStream): File {
        val file = folder.newFile()
        file.outputStream().use { output ->
            inputStream.use { input ->
                input.copyTo(output)
            }
        }
        return file
    }

    protected fun chapters(parser: LocalNovelParser,
                           author: String?, name: String?, introduction: String?
    ): List<LocalNovelChapter> {
        val info = parser.parse()

        Assert.assertEquals(author, info.author)
        Assert.assertEquals(name, info.name)
        Assert.assertEquals(introduction, info.introduction)

        return info.chapters
    }

}