package cc.aoeiuv020.panovel.local

import org.junit.Assert.assertNull
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 *
 * Created by AoEiuV020 on 2017.10.04-16:35:10.
 */
class ExtKtTest {
    @Test
    fun writeObjectNullTest() {
        val bos = ByteArrayOutputStream()
        ObjectOutputStream(bos).apply {
            writeObject(null)
            close()
        }
        val bis = ByteArrayInputStream(bos.toByteArray())
        ObjectInputStream(bis).apply {
            val obj = readObject()
            close()
            assertNull(obj)
            val s = obj as? String
            assertNull(s)
        }
    }
}