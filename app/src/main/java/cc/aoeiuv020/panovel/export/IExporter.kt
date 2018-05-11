package cc.aoeiuv020.panovel.export

import net.lingala.zip4j.core.ZipFile

/**
 * Created by AoEiuV020 on 2018.05.11-17:46:28.
 */
interface IExporter {
    fun import(zipFile: ZipFile, options: Set<ExportOption>): String
    fun export(zipFile: ZipFile, options: Set<ExportOption>): String
}