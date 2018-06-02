package cc.aoeiuv020.irondb.impl

import cc.aoeiuv020.base.jar.type
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by AoEiuV020 on 2018.05.27-16:58:03.
 */
class GsonSerializerTest {
    private val serializer = GsonSerializer()

    @Test
    fun dataClassTest() {
        val student = Student(id = 10L, name = "AoEiuV020", age = 23)
        val json = """{"id":10,"name":"AoEiuV020","age":23}"""
        val string = serializer.serialize(student, type<Student>())
        assertEquals(json, string)
        val obj: Student = serializer.deserialize(string, type<Student>())
        assertEquals(student, obj)
    }

    @Test
    fun listTest() {
        val list = listOf(
                Student(id = 10L, name = "AoEiuV020", age = 23),
                Student(name = "name", age = -1)
        )
        val json = """[{"id":10,"name":"AoEiuV020","age":23},{"name":"name","age":-1}]"""
        val string = serializer.serialize(list, type<List<Student>>())
        assertEquals(json, string)
        val obj: List<Student> = serializer.deserialize(string, type<List<Student>>())
        assertEquals(list, obj)
    }

    data class Student(
            var id: Long? = null,
            val name: String,
            val age: Int
    )
}
