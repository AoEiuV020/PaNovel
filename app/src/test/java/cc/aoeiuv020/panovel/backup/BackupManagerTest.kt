package cc.aoeiuv020.panovel.backup

import net.lingala.zip4j.core.ZipFile
import net.lingala.zip4j.model.FileHeader
import net.lingala.zip4j.model.ZipParameters
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runners.MethodSorters


/**
 * Created by AoEiuV020 on 2018.05.11-18:09:14.
 */
@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
class BackupManagerTest {
    @Rule
    @JvmField
    val folder = TemporaryFolder()


    @Before
    fun setUp() {
    }

    @Test
    fun a1_addVersion() {
        val zipFile: ZipFile = folder.newFile().let {
            it.delete()
            ZipFile(it)
        }
        val p = ZipParameters()
        zipFile.addStream("1".byteInputStream(), p.apply {
            isSourceExternalStream = true
            fileNameInZip = "version"
        })
        val headers: List<FileHeader> = zipFile.fileHeaders.map { it as FileHeader }
        val header = headers[0]
        assertEquals("version", header.fileName)
        zipFile.getInputStream(header).reader().readText().let {
            assertEquals("1", it)
        }
    }

}