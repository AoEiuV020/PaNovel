package cc.aoeiuv020.panovel.local

/**
 * Created by AoEiuV020 on 2018.06.19-22:21:16.
 */
interface LocalNovelExporter {
    fun export(info: LocalNovelInfo, contentProvider: ContentProvider, progressCallback: (Int, Int) -> Unit)
}