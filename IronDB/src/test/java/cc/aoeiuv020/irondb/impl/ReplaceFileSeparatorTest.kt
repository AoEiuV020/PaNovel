package cc.aoeiuv020.irondb.impl

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by AoEiuV020 on 2018.05.27-16:55:57.
 */
class ReplaceFileSeparatorTest {
    private val serializer = ReplaceFileSeparator()

    @Test
    fun serialize() {
        assertEquals("AoEiuV020", serializer.serialize("AoEiuV020"))
        assertEquals("123456", serializer.serialize("123456"))
        assertEquals("中文", serializer.serialize("中文"))
        assertEquals("啊o额iu鱼", serializer.serialize("啊o额iu鱼"))
        assertEquals("123.456", serializer.serialize("123/456"))
        assertEquals("123.456", serializer.serialize("123|456"))
        assertEquals(" \t\n", serializer.serialize(" \t\n"))
        assertEquals("""`~!@#${'$'}%.&*()-_.+..{}\...'....>.<""", serializer.serialize("""`~!@#${'$'}%^&*()-_=+[]{}\|;:'"/?.>,<"""))
    }

    @Test
    fun char() {
        println(ReplaceFileSeparator.NOT_SUPPORT_CHARACTER)
    }
}