package cc.aoeiuv020.panovel.share

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 *
 * Created by AoEiuV020 on 2018.03.07-19:31:38.
 */
class PasteUbuntuTest {
    private lateinit var paste: PasteUbuntu
    private val text = "PasteUbuntuTest"
    @Before
    fun setUp() {
        paste = PasteUbuntu()
    }

    @Test
    fun upload() {
        val link = paste.upload(PasteUbuntu.PasteUbuntuData(text, expiration = Expiration.DAY))
        println(link)
        assertTrue(link.matches(Regex("https://paste.ubuntu.com/p/\\w*/")))
        val receive = paste.download(link)
        assertEquals(text, receive)
    }

    @Test
    fun download() {
        val receive = paste.download("https://paste.ubuntu.com/p/CH3g747q9S/")
        assertEquals("""{
  "list": [
    {
      "author": "二目",
      "name": "放开那个女巫",
      "requester": {
        "type": "cc.aoeiuv020.panovel.api.DetailRequester",
        "extra": "https://book.qidian.com/info/1003306811"
      },
      "site": "起点中文"
    }
  ],
  "name": "bs"
}""", receive)
    }

}