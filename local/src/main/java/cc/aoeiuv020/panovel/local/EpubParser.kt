package cc.aoeiuv020.panovel.local

import java.io.File

/**
 * Created by AoEiuV020 on 2018.06.16-17:10:44.
 */
class EpubParser(file: File) : LocalNovelParser(file) {
    override fun parse(): LocalNovelInfo {
        nl.siegmann.epublib.domain.Book()
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getNovelContent(extra: String): List<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
