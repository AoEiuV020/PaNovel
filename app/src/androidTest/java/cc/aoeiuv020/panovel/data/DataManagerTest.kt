package cc.aoeiuv020.panovel.data

import android.content.Context
import android.support.test.InstrumentationRegistry
import org.junit.Before
import org.junit.Test

/**
 * Created by AoEiuV020 on 2018.05.27-19:56:20.
 */
class DataManagerTest {
    @Before
    fun setUp() {
        val ctx: Context = InstrumentationRegistry.getTargetContext()
        DataManager.init(ctx)
    }

    @Test
    fun getNovelDetail() {
        DataManager.getNovelDetail(1).let {
            println(it)
        }
    }

    @Test
    fun getNovelFromBookList() {
        DataManager.getNovelFromBookList(2).forEach {
            println(it)
        }
    }
}