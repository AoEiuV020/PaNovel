package cc.aoeiuv020.irondb.impl

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by AoEiuV020 on 2018.05.27-16:44:05.
 */
class Md5HaxSerializerTest {
    private val serializer = Md5HaxSerializer()

    @Test
    fun serialize() {
        assertEquals("d9685229a4d0622da3f75e77bfa56d5a", serializer.serialize("AoEiuV020"))
        assertEquals("e10adc3949ba59abbe56e057f20f883e", serializer.serialize("123456"))
        assertEquals("a7bac2239fcdcb3a067903d8077c4a07", serializer.serialize("中文"))
        assertEquals("2249f181c4e28e3b844e5ab57d5dd4b8", serializer.serialize("啊o额iu鱼"))
        assertEquals("206d81c8660a4e7d7b81f175acbdcc8b", serializer.serialize("123/456"))
        assertEquals("09ca1819e6bb545e495dc35feaf40ccf", serializer.serialize(" \t\n"))
        assertEquals("fcbc5d773d0351226348e96536c24448", serializer.serialize("""`~!@#${'$'}%^&*()-_=+[]{}\|;:'"/?.>,<"""))
    }
}