package cc.aoeiuv020.panovel.local

import android.support.test.runner.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 *
 * Created by AoEiuV020 on 2017.10.04-20:59:47.
 */
@RunWith(AndroidJUnit4::class)
class BookshelfKtTest {
    @Test
    fun md5Base64Test() {
        assertEquals("BX_s4PUKDhs99OF-33uIag", md5Base64("飘天文学尹四当个法师闹革命"))
    }
}