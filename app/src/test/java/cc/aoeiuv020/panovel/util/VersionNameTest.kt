package cc.aoeiuv020.panovel.util

import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Created by AoEiuV020 on 2018.05.17-15:56:59.
 */
class VersionNameTest {

    @Test
    fun compareTo() {
        assertTrue("0".v == "0".v)
        assertTrue("1.0.8".v < "1.1.1".v)
        assertTrue("1.1.2".v < "1.1.12".v)
        assertTrue("1.1.2".v < "1.2".v)
        assertTrue("2.2.2".v > "1.3.1".v)
        assertTrue("2.2.2".v > "2.2".v)
        assertTrue("3.4.5.1".v > "3.4.5-2022".v)
        assertTrue("3.4.5".v > "3.4.5-2022".v)
        assertTrue("3.4.5-2022".v > "3.4.5-2021".v)
        assertTrue("3.4.5-2022".v > "3.4.4".v)
        assertTrue("3.4.5-2022".v > "3.4.4.4".v)
    }

    private val String.v get() = VersionName(this)
}