package cc.aoeiuv020.panovel.export.impl

import cc.aoeiuv020.panovel.export.ExportOption
import org.jetbrains.anko.info
import java.io.File

/**
 * Created by AoEiuV020 on 2018.05.30-20:40:56.
 */
class ExporterV2 : DefaultExporter() {
    override fun import(file: File, option: ExportOption): Int {
        info {
            file.listFiles()
        }
        return 0
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun export(file: File, option: ExportOption): Int {
        info {
            file.listFiles()
        }
        return 0
    }
}