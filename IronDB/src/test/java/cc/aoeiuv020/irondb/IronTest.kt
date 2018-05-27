package cc.aoeiuv020.irondb

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * Created by AoEiuV020 on 2018.05.27-17:12:07.
 */
class IronTest {
    @Rule
    @JvmField
    val folder = TemporaryFolder()
    private lateinit var root: File
    private lateinit var database: Database

    @Before
    fun setUp() {
        root = folder.newFolder()
        database = Iron.db(root)
    }

    @Test
    fun dataClassTest() {
        val student = Student(id = 10L, name = "AoEiuV020", age = 23)
        assertFalse(database.keysContainer().contains("student"))
        database.write("student", student)
        assertTrue(database.keysContainer().contains("student"))
        val obj: Student = requireNotNull(database.read("student"))
        assertEquals(student, obj)
    }

    @Test
    fun listTest() {
        val list = listOf(
                Student(id = 10L, name = "AoEiuV020", age = 23),
                Student(name = "name", age = -1)
        )
        assertFalse(database.keysContainer().contains("list"))
        database.write("list", list)
        assertTrue(database.keysContainer().contains("list"))
        val obj: List<Student> = requireNotNull(database.read("list"))
        assertEquals(list, obj)
    }

    @Test
    fun subTest() {
        val db = database.sub("sub")
        val student = Student(id = 10L, name = "AoEiuV020", age = 23)
        assertFalse(db.keysContainer().contains("student"))
        db.write("student", student)
        assertTrue(db.keysContainer().contains("student"))
        val obj: Student = requireNotNull(db.read("student"))
        assertEquals(student, obj)
        assertTrue(database.keysContainer().contains("sub"))
    }

    data class Student(
            var id: Long? = null,
            val name: String,
            val age: Int
    )
}
