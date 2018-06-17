package cc.aoeiuv020.panovel.local

import net.sf.jazzlib.ZipFile
import nl.siegmann.epublib.epub.EpubReader
import org.junit.Test

/**
 * Created by AoEiuV020 on 2018.06.16-17:52:10.
 */
class EpubParserTest: ParserTest(EpubParser::class) {

    @Test
    fun parse() {
        val file = getFile("/home/aoeiuv/tmp/panovel/epub/打工吧！魔王大人17.epub") ?: return
        val zipFile = ZipFile(file)
        val book =  EpubReader().readEpub(zipFile)
        book.contents.forEach {
            println(it)
        }
    }
}