package cc.aoeiuv020.panovel.local

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by AoEiuV020 on 2018.05.14-11:46:55.
 */
@RunWith(AndroidJUnit4::class)
class CheckTest {

    lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getTargetContext()
    }

    @Test
    fun curChangeLog() {
        Check.getChangeLogFromAssert(context, "2.1.1").let {
            println(it)
        }
    }

}