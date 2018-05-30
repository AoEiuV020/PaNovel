package cc.aoeiuv020.panovel.export

import java.io.File

/**
 * Created by AoEiuV020 on 2018.05.11-17:46:28.
 */
interface IExporter {
    fun import(base: File, options: Set<ExportOption>): String
    fun export(base: File, options: Set<ExportOption>): String
}